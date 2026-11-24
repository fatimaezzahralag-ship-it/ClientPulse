package ma.ensam.clientpulse.service.impl;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.CommercialDao;
import ma.ensam.clientpulse.dao.interfaces.JournalImportDao;
import ma.ensam.clientpulse.dao.interfaces.UtilisateurDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Commercial;
import ma.ensam.clientpulse.domain.entity.JournalImport;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;
import ma.ensam.clientpulse.dto.JournalImportDto;
import ma.ensam.clientpulse.dto.LigneRejeteeDto;
import ma.ensam.clientpulse.dto.RapportImportDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.service.interfaces.CsvImportService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Import CSV des clients : contrôle, nettoyage, création, journalisation.
 *
 * Format accepté : séparateur « ; » ou « , » (détecté sur l'en-tête), encodage UTF-8 ou
 * Windows-1252 (export Excel). Colonnes obligatoires : code, raison_sociale, type.
 * Colonnes facultatives : ice, email, telephone, adresse, ville, secteur,
 * delai_paiement_jours (30 par défaut), statut (ACTIF par défaut), commercial_email.
 *
 * Contrôles par ligne (règles alignées sur {@code ClientRequest}) : champs obligatoires,
 * longueurs maximales, type et statut connus, e-mail valide, ICE à 15 chiffres, délai de
 * paiement entre 0 et 180, commercial existant, code non dupliqué (dans le fichier et en base).
 * Nettoyage : espaces superflus supprimés, code en majuscules, e-mail en minuscules,
 * téléphone sans espaces ni séparateurs.
 *
 * Une ligne invalide n'empêche pas l'import des autres : elle est listée dans le rapport.
 */
@Service
@Transactional
public class CsvImportServiceImpl implements CsvImportService {

    static final int MAX_LIGNES = 5000;
    static final int MAX_REJETS_DETAILLES = 200;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern ICE = Pattern.compile("^\\d{15}$");

    private static final List<String> COLONNES_OBLIGATOIRES = List.of("code", "raison_sociale", "type");

    /** Noms de colonnes tolérés -> nom canonique. */
    private static final Map<String, String> ALIAS = Map.ofEntries(
            Map.entry("raisonsociale", "raison_sociale"),
            Map.entry("nom", "raison_sociale"),
            Map.entry("telephone", "telephone"),
            Map.entry("tel", "telephone"),
            Map.entry("delai_paiement", "delai_paiement_jours"),
            Map.entry("delai", "delai_paiement_jours"),
            Map.entry("delaipaiementjours", "delai_paiement_jours"),
            Map.entry("commercial", "commercial_email"),
            Map.entry("email_commercial", "commercial_email")
    );

    private static final String MODELE =
            "code;raison_sociale;ice;type;email;telephone;adresse;ville;secteur;"
                    + "delai_paiement_jours;statut;commercial_email\n"
                    + "CLI-0001;Atlas Négoce SARL;001234567000089;ENTREPRISE;contact@atlas-negoce.ma;"
                    + "+212522000000;12 rue des Fleurs;Casablanca;Commerce;30;ACTIF;commercial@clientpulse.ma\n";

    private final ClientDao clientDao;
    private final CommercialDao commercialDao;
    private final UtilisateurDao utilisateurDao;
    private final JournalImportDao journalImportDao;

    public CsvImportServiceImpl(ClientDao clientDao,
                                CommercialDao commercialDao,
                                UtilisateurDao utilisateurDao,
                                JournalImportDao journalImportDao) {
        this.clientDao = clientDao;
        this.commercialDao = commercialDao;
        this.utilisateurDao = utilisateurDao;
        this.journalImportDao = journalImportDao;
    }

    // ------------------------------------------------------------------ API

    @Override
    public RapportImportDto importerClients(String nomFichier, InputStream contenu) {
        String texte = lireTexte(contenu);
        List<String[]> lignes = decouper(texte);

        if (lignes.isEmpty()) {
            throw new RegleMetierException("Le fichier est vide.");
        }
        Map<String, Integer> colonnes = indexerEntetes(lignes.get(0));
        verifierColonnesObligatoires(colonnes);

        int totalDonnees = lignes.size() - 1;
        if (totalDonnees <= 0) {
            throw new RegleMetierException("Le fichier ne contient aucune ligne de données.");
        }
        if (totalDonnees > MAX_LIGNES) {
            throw new RegleMetierException("Le fichier dépasse " + MAX_LIGNES + " lignes : découpez-le en plusieurs imports.");
        }

        Set<String> codesImportes = new HashSet<>();
        List<LigneRejeteeDto> rejets = new ArrayList<>();
        int lues = 0;
        int importees = 0;
        int rejetees = 0;

        for (int i = 1; i < lignes.size(); i++) {
            String[] brut = lignes.get(i);
            if (estVide(brut)) {
                continue;   // ligne vide en fin de fichier ou entre deux blocs
            }
            lues++;
            int numeroLigne = i + 1;   // l'en-tête est la ligne 1
            try {
                Client client = construireClient(brut, colonnes, codesImportes);
                clientDao.save(client);
                codesImportes.add(client.getCode());
                importees++;
            } catch (LigneInvalide invalide) {
                rejetees++;
                if (rejets.size() < MAX_REJETS_DETAILLES) {
                    rejets.add(new LigneRejeteeDto(numeroLigne, invalide.code, invalide.getMessage()));
                }
            }
        }

        JournalImport journal = new JournalImport();
        journal.setFichier(nettoyerNomFichier(nomFichier));
        journal.setLignesLues(lues);
        journal.setLignesImportees(importees);
        journal.setLignesRejetees(rejetees);
        JournalImport enregistre = journalImportDao.save(journal);

        return new RapportImportDto(
                enregistre.getId(), enregistre.getFichier(), lues, importees, rejetees, rejets);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JournalImportDto> historique(Pageable pageable) {
        return journalImportDao.findAll(pageable).map(journal -> new JournalImportDto(
                journal.getId(),
                journal.getFichier(),
                journal.getLignesLues(),
                journal.getLignesImportees(),
                journal.getLignesRejetees(),
                journal.getDateCreation(),
                journal.getCreePar()));
    }

    @Override
    public String modeleClients() {
        return MODELE;
    }

    // ------------------------------------------------------------------ lecture du fichier

    /** UTF-8 d'abord ; si le fichier n'est pas de l'UTF-8 valide (export Excel), Windows-1252. */
    private static String lireTexte(InputStream contenu) {
        byte[] octets;
        try {
            octets = contenu.readAllBytes();
        } catch (IOException e) {
            throw new RegleMetierException("Le fichier est illisible.");
        }
        String texte;
        try {
            texte = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(octets))
                    .toString();
        } catch (CharacterCodingException e) {
            texte = new String(octets, java.nio.charset.Charset.forName("windows-1252"));
        }
        if (!texte.isEmpty() && texte.charAt(0) == '\uFEFF') {
            texte = texte.substring(1);   // BOM ajouté par Excel
        }
        return texte;
    }

    private static List<String[]> decouper(String texte) {
        if (texte.isBlank()) {
            return List.of();
        }
        char separateur = detecterSeparateur(texte);
        List<String[]> lignes = new ArrayList<>();
        try (CSVReader lecteur = new CSVReaderBuilder(new StringReader(texte))
                .withCSVParser(new CSVParserBuilder().withSeparator(separateur).build())
                .build()) {
            String[] ligne;
            while ((ligne = lecteur.readNext()) != null) {
                lignes.add(ligne);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RegleMetierException("Le fichier CSV est mal formé : " + e.getMessage());
        }
        return lignes;
    }

    /** « ; » si la première ligne en contient plus que de « , » (cas des exports Excel en français). */
    private static char detecterSeparateur(String texte) {
        int fin = texte.indexOf('\n');
        String premiere = fin < 0 ? texte : texte.substring(0, fin);
        long pointsVirgules = premiere.chars().filter(c -> c == ';').count();
        long virgules = premiere.chars().filter(c -> c == ',').count();
        return pointsVirgules >= virgules && pointsVirgules > 0 ? ';' : ',';
    }

    private static Map<String, Integer> indexerEntetes(String[] entetes) {
        Map<String, Integer> colonnes = new HashMap<>();
        for (int i = 0; i < entetes.length; i++) {
            String nom = normaliserEntete(entetes[i]);
            if (!nom.isEmpty()) {
                colonnes.putIfAbsent(ALIAS.getOrDefault(nom, nom), i);
            }
        }
        return colonnes;
    }

    private static String normaliserEntete(String entete) {
        String sansAccent = Normalizer.normalize(entete == null ? "" : entete, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sansAccent.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static void verifierColonnesObligatoires(Map<String, Integer> colonnes) {
        List<String> manquantes = COLONNES_OBLIGATOIRES.stream()
                .filter(nom -> !colonnes.containsKey(nom))
                .toList();
        if (!manquantes.isEmpty()) {
            throw new RegleMetierException("Colonnes obligatoires manquantes : " + String.join(", ", manquantes)
                    + ". Téléchargez le modèle CSV pour connaître le format attendu.");
        }
    }

    private static boolean estVide(String[] ligne) {
        for (String cellule : ligne) {
            if (cellule != null && !cellule.isBlank()) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ contrôle et nettoyage d'une ligne

    private Client construireClient(String[] brut, Map<String, Integer> colonnes, Set<String> codesImportes) {
        String code = nettoyer(valeur(brut, colonnes, "code")).toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            throw new LigneInvalide("", "Le code client est obligatoire.");
        }
        if (code.length() > 30) {
            throw new LigneInvalide(code, "Le code client dépasse 30 caractères.");
        }
        if (codesImportes.contains(code)) {
            throw new LigneInvalide(code, "Code en doublon dans le fichier.");
        }
        if (clientDao.existsByCode(code)) {
            throw new LigneInvalide(code, "Un client avec ce code existe déjà.");
        }

        String raisonSociale = nettoyer(valeur(brut, colonnes, "raison_sociale"));
        if (raisonSociale.isEmpty()) {
            throw new LigneInvalide(code, "La raison sociale est obligatoire.");
        }
        if (raisonSociale.length() > 150) {
            throw new LigneInvalide(code, "La raison sociale dépasse 150 caractères.");
        }

        TypeClient type = lireType(code, valeur(brut, colonnes, "type"));
        StatutClient statut = lireStatut(code, valeur(brut, colonnes, "statut"));

        String ice = nettoyer(valeur(brut, colonnes, "ice")).replaceAll("[\\s.-]", "");
        if (!ice.isEmpty() && !ICE.matcher(ice).matches()) {
            throw new LigneInvalide(code, "L'ICE doit comporter exactement 15 chiffres.");
        }

        String email = nettoyer(valeur(brut, colonnes, "email")).toLowerCase(Locale.ROOT);
        if (!email.isEmpty() && (email.length() > 150 || !EMAIL.matcher(email).matches())) {
            throw new LigneInvalide(code, "L'adresse e-mail est invalide.");
        }

        String telephone = nettoyer(valeur(brut, colonnes, "telephone")).replaceAll("[\\s.\\-()]", "");
        if (telephone.length() > 30) {
            throw new LigneInvalide(code, "Le téléphone dépasse 30 caractères.");
        }

        String adresse = limiter(code, "L'adresse", nettoyer(valeur(brut, colonnes, "adresse")), 255);
        String ville = limiter(code, "La ville", nettoyer(valeur(brut, colonnes, "ville")), 80);
        String secteur = limiter(code, "Le secteur", nettoyer(valeur(brut, colonnes, "secteur")), 80);

        int delai = lireDelai(code, valeur(brut, colonnes, "delai_paiement_jours"));
        Commercial commercial = lireCommercial(code, valeur(brut, colonnes, "commercial_email"));

        Client client = new Client();
        client.setCode(code);
        client.setRaisonSociale(raisonSociale);
        client.setIce(vide(ice));
        client.setType(type);
        client.setStatut(statut);
        client.setEmail(vide(email));
        client.setTelephone(vide(telephone));
        client.setAdresse(vide(adresse));
        client.setVille(vide(ville));
        client.setSecteur(vide(secteur));
        client.setDelaiPaiementJours(delai);
        client.setCommercial(commercial);
        return client;
    }

    private static TypeClient lireType(String code, String brut) {
        String valeur = sansAccent(nettoyer(brut)).toUpperCase(Locale.ROOT);
        if (valeur.isEmpty()) {
            throw new LigneInvalide(code, "Le type est obligatoire (ENTREPRISE ou PARTICULIER).");
        }
        try {
            return TypeClient.valueOf(valeur);
        } catch (IllegalArgumentException e) {
            throw new LigneInvalide(code, "Type inconnu « " + brut.trim() + " » (ENTREPRISE ou PARTICULIER).");
        }
    }

    private static StatutClient lireStatut(String code, String brut) {
        String valeur = sansAccent(nettoyer(brut)).toUpperCase(Locale.ROOT);
        if (valeur.isEmpty()) {
            return StatutClient.ACTIF;
        }
        try {
            return StatutClient.valueOf(valeur);
        } catch (IllegalArgumentException e) {
            throw new LigneInvalide(code, "Statut inconnu « " + brut.trim() + " » (PROSPECT, ACTIF ou INACTIF).");
        }
    }

    private static int lireDelai(String code, String brut) {
        String valeur = nettoyer(brut);
        if (valeur.isEmpty()) {
            return 30;
        }
        try {
            int delai = Integer.parseInt(valeur);
            if (delai < 0 || delai > 180) {
                throw new LigneInvalide(code, "Le délai de paiement doit être compris entre 0 et 180 jours.");
            }
            return delai;
        } catch (NumberFormatException e) {
            throw new LigneInvalide(code, "Le délai de paiement n'est pas un nombre entier.");
        }
    }

    private Commercial lireCommercial(String code, String brut) {
        String email = nettoyer(brut).toLowerCase(Locale.ROOT);
        if (email.isEmpty()) {
            return null;
        }
        return utilisateurDao.findByEmail(email)
                .flatMap(utilisateur -> commercialDao.findByUtilisateurId(utilisateur.getId()))
                .orElseThrow(() -> new LigneInvalide(code, "Aucun commercial trouvé pour l'e-mail « " + email + " »."));
    }

    // ------------------------------------------------------------------ petits utilitaires

    private static String valeur(String[] ligne, Map<String, Integer> colonnes, String nom) {
        Integer index = colonnes.get(nom);
        if (index == null || index >= ligne.length || ligne[index] == null) {
            return "";
        }
        return ligne[index];
    }

    /** Supprime les espaces de début et de fin, réduit les espaces multiples, retire les insécables. */
    private static String nettoyer(String texte) {
        return texte == null ? "" : texte.replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
    }

    private static String sansAccent(String texte) {
        return Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static String vide(String texte) {
        return texte.isEmpty() ? null : texte;
    }

    private static String limiter(String code, String champ, String valeur, int max) {
        if (valeur.length() > max) {
            throw new LigneInvalide(code, champ + " dépasse " + max + " caractères.");
        }
        return valeur;
    }

    private static String nettoyerNomFichier(String nom) {
        if (nom == null || nom.isBlank()) {
            return "import.csv";
        }
        String simple = nom.replace('\\', '/');
        simple = simple.substring(simple.lastIndexOf('/') + 1).trim();
        if (simple.isEmpty()) {
            return "import.csv";
        }
        return simple.length() > 255 ? simple.substring(0, 255) : simple;
    }

    /** Erreur interne : une ligne ne respecte pas les règles, elle est rejetée sans arrêter l'import. */
    private static final class LigneInvalide extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final String code;

        private LigneInvalide(String code, String message) {
            super(message, null, false, false);
            this.code = code;
        }
    }
}

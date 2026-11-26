package ma.ensam.clientpulse.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeFacture;
import ma.ensam.clientpulse.dto.DevisDto;
import ma.ensam.clientpulse.dto.DocumentPdfDto;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.LigneDto;
import ma.ensam.clientpulse.dto.PaiementDto;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.DevisService;
import ma.ensam.clientpulse.service.interfaces.FacturationService;
import ma.ensam.clientpulse.service.interfaces.PdfService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Devis et factures en PDF avec OpenPDF.
 *
 * Polices Helvetica (jeu WinAnsi : accents français corrects). Mise en page : identité de l'émetteur,
 * bloc client, tableau des lignes, totaux HT / TVA par taux / TTC, puis, pour une facture, les
 * paiements reçus et le reste à payer. Les montants viennent des DTO déjà calculés par les services
 * (BigDecimal, 2 décimales) : le PDF n'invente aucun calcul, il se contente de les afficher, sauf pour
 * un montant de ligne absent (brouillon) qu'il recalcule avec la même formule.
 */
@Service
@Transactional(readOnly = true)
public class PdfServiceImpl implements PdfService {

    private static final Color BORDEAUX = new Color(0x7B, 0x22, 0x33);
    private static final Color GRIS_CLAIR = new Color(0xF2, 0xF2, 0xF2);
    private static final Color GRIS_TEXTE = new Color(0x55, 0x55, 0x55);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private final DevisService devisService;
    private final FacturationService facturationService;
    private final ClientDao clientDao;

    private final String entrepriseNom;
    private final String entrepriseAdresse;
    private final String entrepriseIce;
    private final String entrepriseTelephone;
    private final String entrepriseEmail;

    public PdfServiceImpl(DevisService devisService,
                          FacturationService facturationService,
                          ClientDao clientDao,
                          @Value("${app.entreprise.nom:ClientPulse SARL}") String entrepriseNom,
                          @Value("${app.entreprise.adresse:Casablanca, Maroc}") String entrepriseAdresse,
                          @Value("${app.entreprise.ice:000000000000000}") String entrepriseIce,
                          @Value("${app.entreprise.telephone:}") String entrepriseTelephone,
                          @Value("${app.entreprise.email:}") String entrepriseEmail) {
        this.devisService = devisService;
        this.facturationService = facturationService;
        this.clientDao = clientDao;
        this.entrepriseNom = entrepriseNom;
        this.entrepriseAdresse = entrepriseAdresse;
        this.entrepriseIce = entrepriseIce;
        this.entrepriseTelephone = entrepriseTelephone;
        this.entrepriseEmail = entrepriseEmail;
    }

    // ------------------------------------------------------------------ API

    @Override
    public DocumentPdfDto genererDevis(Long devisId) {
        DevisDto devis = devisService.trouver(devisId);
        Client client = chargerClient(devis.clientId());

        String numero = devis.numero() == null ? "brouillon" : devis.numero();
        byte[] contenu = construire("DEVIS", numero, List.of(
                "Date d'émission : " + date(devis.dateEmission()),
                "Valable jusqu'au : " + date(devis.dateValidite()),
                "Statut : " + libelleStatut(String.valueOf(devis.statut()))),
                client, devis.lignes(), devis.totalHT(), devis.totalTVA(), devis.totalTTC(),
                null, null, null,
                "Devis valable jusqu'à la date indiquée. Bon pour accord : date, signature et cachet du client.");
        return new DocumentPdfDto("devis-" + nomSur(numero) + ".pdf", contenu);
    }

    @Override
    public DocumentPdfDto genererFacture(Long factureId) {
        FactureDto facture = facturationService.trouver(factureId);
        Client client = chargerClient(facture.clientId());

        boolean avoir = facture.type() == TypeFacture.AVOIR;
        boolean brouillon = facture.statut() == StatutFacture.BROUILLON;
        String titre = avoir ? "AVOIR" : "FACTURE";
        String numero = facture.numero() == null ? "brouillon" : facture.numero();

        List<String> infos = new java.util.ArrayList<>();
        if (brouillon) {
            infos.add("BROUILLON : document non émis, sans valeur comptable");
        }
        infos.add("Date d'émission : " + date(facture.dateEmission()));
        if (!avoir) {
            infos.add("Date d'échéance : " + date(facture.dateEcheance()));
        }
        infos.add("Statut : " + libelleStatut(String.valueOf(facture.statut())));

        String pied = avoir
                ? "Avoir établi en correction d'une facture émise."
                : "Conditions de paiement : " + conditions(client)
                  + ". Toute somme non réglée à l'échéance peut donner lieu aux relances de recouvrement.";

        byte[] contenu = construire(titre, numero, infos, client, facture.lignes(),
                facture.totalHT(), facture.totalTVA(), facture.totalTTC(),
                avoir ? null : facture.paiements(),
                avoir ? null : facture.totalPaye(),
                avoir ? null : facture.resteAPayer(),
                pied);
        String prefixe = avoir ? "avoir-" : "facture-";
        return new DocumentPdfDto(prefixe + nomSur(numero) + ".pdf", contenu);
    }

    // ------------------------------------------------------------------ construction du document

    private byte[] construire(String titre, String numero, List<String> infosDocument, Client client,
                              List<LigneDto> lignes, BigDecimal totalHT, BigDecimal totalTVA, BigDecimal totalTTC,
                              List<PaiementDto> paiements, BigDecimal totalPaye, BigDecimal resteAPayer,
                              String pied) {
        DecimalFormat montants = formatMontants();
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 50);
        try {
            PdfWriter.getInstance(document, sortie);
            document.addTitle(titre + " " + numero);
            document.addAuthor(entrepriseNom);
            document.open();

            document.add(entete(titre, numero, infosDocument));
            document.add(espace(10));
            document.add(blocClient(client));
            document.add(espace(14));
            document.add(tableauLignes(lignes, montants));
            document.add(espace(10));
            document.add(totaux(lignes, totalHT, totalTVA, totalTTC, montants));

            if (paiements != null) {
                document.add(espace(12));
                document.add(blocPaiements(paiements, totalPaye, resteAPayer, montants));
            }
            if (pied != null) {
                document.add(espace(18));
                Paragraph mention = new Paragraph(pied, police(8, Font.ITALIC, GRIS_TEXTE));
                document.add(mention);
            }
            document.add(espace(10));
            Paragraph identite = new Paragraph(
                    entrepriseNom + " · ICE " + entrepriseIce + " · " + entrepriseAdresse,
                    police(7, Font.NORMAL, GRIS_TEXTE));
            identite.setAlignment(Element.ALIGN_CENTER);
            document.add(identite);
        } catch (DocumentException e) {
            throw new IllegalStateException("Génération du PDF impossible", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return sortie.toByteArray();
    }

    private PdfPTable entete(String titre, String numero, List<String> infosDocument) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1.2f, 1f});
        table.setWidthPercentage(100);

        PdfPCell emetteur = cellule();
        emetteur.addElement(new Paragraph(entrepriseNom, police(14, Font.BOLD, BORDEAUX)));
        emetteur.addElement(new Paragraph(entrepriseAdresse, police(8, Font.NORMAL, GRIS_TEXTE)));
        emetteur.addElement(new Paragraph("ICE : " + entrepriseIce, police(8, Font.NORMAL, GRIS_TEXTE)));
        if (!entrepriseTelephone.isBlank()) {
            emetteur.addElement(new Paragraph("Tél. : " + entrepriseTelephone, police(8, Font.NORMAL, GRIS_TEXTE)));
        }
        if (!entrepriseEmail.isBlank()) {
            emetteur.addElement(new Paragraph(entrepriseEmail, police(8, Font.NORMAL, GRIS_TEXTE)));
        }
        table.addCell(emetteur);

        PdfPCell document = cellule();
        Paragraph t = new Paragraph(titre, police(20, Font.BOLD, BORDEAUX));
        t.setAlignment(Element.ALIGN_RIGHT);
        document.addElement(t);
        Paragraph n = new Paragraph("N° " + numero, police(11, Font.BOLD, Color.BLACK));
        n.setAlignment(Element.ALIGN_RIGHT);
        document.addElement(n);
        for (String info : infosDocument) {
            Paragraph p = new Paragraph(info, police(8, Font.NORMAL, Color.BLACK));
            p.setAlignment(Element.ALIGN_RIGHT);
            document.addElement(p);
        }
        table.addCell(document);
        return table;
    }

    private PdfPTable blocClient(Client client) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(55);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell cellule = new PdfPCell();
        cellule.setBorderColor(BORDEAUX);
        cellule.setPadding(8);
        cellule.addElement(new Paragraph("CLIENT", police(7, Font.BOLD, BORDEAUX)));
        cellule.addElement(new Paragraph(client.getRaisonSociale(), police(11, Font.BOLD, Color.BLACK)));
        ajouterLigne(cellule, "Code : " + client.getCode());
        if (client.getIce() != null && !client.getIce().isBlank()) {
            ajouterLigne(cellule, "ICE : " + client.getIce());
        }
        if (client.getAdresse() != null && !client.getAdresse().isBlank()) {
            ajouterLigne(cellule, client.getAdresse());
        }
        if (client.getVille() != null && !client.getVille().isBlank()) {
            ajouterLigne(cellule, client.getVille());
        }
        if (client.getEmail() != null && !client.getEmail().isBlank()) {
            ajouterLigne(cellule, client.getEmail());
        }
        table.addCell(cellule);
        return table;
    }

    private PdfPTable tableauLignes(List<LigneDto> lignes, DecimalFormat montants) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{4.2f, 1f, 1.5f, 1f, 1f, 1.7f});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        String[] entetes = {"Désignation", "Qté", "PU HT", "Remise", "TVA", "Total HT"};
        for (int i = 0; i < entetes.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(entetes[i], police(8, Font.BOLD, Color.WHITE)));
            c.setBackgroundColor(BORDEAUX);
            c.setBorder(Rectangle.NO_BORDER);
            c.setPadding(5);
            c.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(c);
        }

        boolean alterne = false;
        for (LigneDto ligne : lignes) {
            Color fond = alterne ? GRIS_CLAIR : Color.WHITE;
            table.addCell(ligneCellule(ligne.designation(), Element.ALIGN_LEFT, fond));
            table.addCell(ligneCellule(quantite(ligne.quantite()), Element.ALIGN_RIGHT, fond));
            table.addCell(ligneCellule(montants.format(valeur(ligne.prixUnitaireHT())), Element.ALIGN_RIGHT, fond));
            table.addCell(ligneCellule(valeur(ligne.remise()).signum() == 0 ? "-" : taux(ligne.remise()),
                    Element.ALIGN_RIGHT, fond));
            table.addCell(ligneCellule(taux(ligne.tauxTVA()), Element.ALIGN_RIGHT, fond));
            table.addCell(ligneCellule(montants.format(montantHt(ligne)), Element.ALIGN_RIGHT, fond));
            alterne = !alterne;
        }
        return table;
    }

    private PdfPTable totaux(List<LigneDto> lignes, BigDecimal totalHT, BigDecimal totalTVA, BigDecimal totalTTC,
                             DecimalFormat montants) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{2f, 1.2f});
        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        ligneTotal(table, "Total HT", montants.format(valeur(totalHT)), false);
        for (Map.Entry<BigDecimal, BigDecimal> tva : tvaParTaux(lignes).entrySet()) {
            ligneTotal(table, "TVA " + taux(tva.getKey()), montants.format(tva.getValue()), false);
        }
        ligneTotal(table, "Total TVA", montants.format(valeur(totalTVA)), false);
        ligneTotal(table, "Total TTC (MAD)", montants.format(valeur(totalTTC)), true);
        return table;
    }

    private PdfPTable blocPaiements(List<PaiementDto> paiements, BigDecimal totalPaye, BigDecimal resteAPayer,
                                    DecimalFormat montants) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1.3f, 1.3f, 2f, 1.5f});
        table.setWidthPercentage(100);
        String[] entetes = {"Paiement du", "Mode", "Référence", "Montant"};
        for (int i = 0; i < entetes.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(entetes[i], police(8, Font.BOLD, BORDEAUX)));
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(BORDEAUX);
            c.setPadding(4);
            c.setHorizontalAlignment(i == 3 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            table.addCell(c);
        }
        if (paiements.isEmpty()) {
            PdfPCell vide = new PdfPCell(new Phrase("Aucun paiement enregistré.", police(8, Font.ITALIC, GRIS_TEXTE)));
            vide.setColspan(4);
            vide.setBorder(Rectangle.NO_BORDER);
            vide.setPadding(4);
            table.addCell(vide);
        }
        for (PaiementDto p : paiements) {
            table.addCell(ligneCellule(date(p.datePaiement()), Element.ALIGN_LEFT, Color.WHITE));
            table.addCell(ligneCellule(p.mode() == null ? "" : p.mode().name(), Element.ALIGN_LEFT, Color.WHITE));
            table.addCell(ligneCellule(p.reference() == null ? "" : p.reference(), Element.ALIGN_LEFT, Color.WHITE));
            table.addCell(ligneCellule(montants.format(valeur(p.montant())), Element.ALIGN_RIGHT, Color.WHITE));
        }

        PdfPTable synthese = new PdfPTable(new float[]{2f, 1.2f});
        synthese.setWidthPercentage(45);
        synthese.setHorizontalAlignment(Element.ALIGN_RIGHT);
        synthese.setSpacingBefore(6);
        ligneTotal(synthese, "Déjà payé", montants.format(valeur(totalPaye)), false);
        ligneTotal(synthese, "Reste à payer (MAD)", montants.format(valeur(resteAPayer)), true);

        PdfPTable conteneur = new PdfPTable(1);
        conteneur.setWidthPercentage(100);
        PdfPCell a = cellule();
        a.addElement(table);
        a.addElement(synthese);
        conteneur.addCell(a);
        return conteneur;
    }

    // ------------------------------------------------------------------ calculs d'affichage

    /** TVA regroupée par taux, d'après les montants de ligne (recalculés si absents). Ordre croissant des taux. */
    static Map<BigDecimal, BigDecimal> tvaParTaux(List<LigneDto> lignes) {
        Map<BigDecimal, BigDecimal> regroupe = new TreeMap<>();
        for (LigneDto ligne : lignes) {
            BigDecimal tauxLigne = valeur(ligne.tauxTVA()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal tva = ligne.montantTVA() != null
                    ? ligne.montantTVA()
                    : montantHt(ligne).multiply(tauxLigne).divide(CENT, 2, RoundingMode.HALF_UP);
            regroupe.merge(tauxLigne, tva, BigDecimal::add);
        }
        return regroupe;
    }

    /** HT de la ligne : valeur stockée, sinon quantité x prix unitaire x (1 - remise %), arrondi HALF_UP. */
    static BigDecimal montantHt(LigneDto ligne) {
        if (ligne.montantHT() != null) {
            return ligne.montantHT();
        }
        BigDecimal brut = valeur(ligne.quantite()).multiply(valeur(ligne.prixUnitaireHT()));
        BigDecimal remise = valeur(ligne.remise());
        BigDecimal net = brut.multiply(CENT.subtract(remise)).divide(CENT, 2, RoundingMode.HALF_UP);
        return net.setScale(2, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------ utilitaires

    private Client chargerClient(Long clientId) {
        return clientDao.findById(clientId)
                .orElseThrow(() -> new RessourceIntrouvableException("Client introuvable : " + clientId));
    }

    private static String conditions(Client client) {
        Integer delai = client.getDelaiPaiementJours();
        return delai == null || delai == 0 ? "paiement à réception" : "paiement à " + delai + " jours";
    }

    private static String libelleStatut(String statut) {
        return statut.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    private static String date(LocalDate date) {
        return date == null ? "-" : DATE.format(date);
    }

    private static String taux(BigDecimal valeur) {
        BigDecimal v = valeur(valeur).stripTrailingZeros();
        return (v.scale() < 0 ? v.setScale(0) : v).toPlainString() + " %";
    }

    private static String quantite(BigDecimal valeur) {
        BigDecimal v = valeur(valeur).stripTrailingZeros();
        return (v.scale() < 0 ? v.setScale(0) : v).toPlainString();
    }

    private static BigDecimal valeur(BigDecimal montant) {
        return montant == null ? BigDecimal.ZERO : montant;
    }

    /** Nom de fichier sûr : lettres, chiffres, tiret et souligné uniquement. */
    private static String nomSur(String numero) {
        return numero.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** Espace insécable fin remplacé par une espace normale : Helvetica WinAnsi ne sait pas l'afficher. */
    private static DecimalFormat formatMontants() {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        symboles.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00", symboles);
    }

    private static Font police(float taille, int style, Color couleur) {
        return FontFactory.getFont(FontFactory.HELVETICA, taille, style, couleur);
    }

    private static Paragraph espace(float hauteur) {
        Paragraph p = new Paragraph(" ", police(1, Font.NORMAL, Color.WHITE));
        p.setSpacingBefore(hauteur);
        return p;
    }

    private static PdfPCell cellule() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static PdfPCell ligneCellule(String texte, int alignement, Color fond) {
        PdfPCell c = new PdfPCell(new Phrase(texte == null ? "" : texte, police(8.5f, Font.NORMAL, Color.BLACK)));
        c.setBorder(Rectangle.NO_BORDER);
        c.setBackgroundColor(fond);
        c.setPadding(5);
        c.setHorizontalAlignment(alignement);
        return c;
    }

    private static void ligneTotal(PdfPTable table, String libelle, String montant, boolean fort) {
        Font f = police(fort ? 10 : 9, fort ? Font.BOLD : Font.NORMAL, fort ? Color.WHITE : Color.BLACK);
        PdfPCell a = new PdfPCell(new Phrase(libelle, f));
        PdfPCell b = new PdfPCell(new Phrase(montant, f));
        for (PdfPCell c : new PdfPCell[]{a, b}) {
            c.setBorder(Rectangle.NO_BORDER);
            c.setPadding(5);
            c.setBackgroundColor(fort ? BORDEAUX : GRIS_CLAIR);
        }
        a.setHorizontalAlignment(Element.ALIGN_LEFT);
        b.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(a);
        table.addCell(b);
    }

    private static void ajouterLigne(PdfPCell cellule, String texte) {
        cellule.addElement(new Paragraph(texte, police(8.5f, Font.NORMAL, Color.BLACK)));
    }
}

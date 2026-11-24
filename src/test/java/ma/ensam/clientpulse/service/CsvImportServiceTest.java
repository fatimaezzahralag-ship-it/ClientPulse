package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.CommercialDao;
import ma.ensam.clientpulse.dao.interfaces.JournalImportDao;
import ma.ensam.clientpulse.dao.interfaces.UtilisateurDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.JournalImport;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;
import ma.ensam.clientpulse.dto.RapportImportDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.service.impl.CsvImportServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsvImportServiceTest {

    private static final String ENTETE =
            "code;raison_sociale;ice;type;email;telephone;adresse;ville;secteur;delai_paiement_jours;statut;commercial_email\n";

    private ClientDao clientDao;
    private CommercialDao commercialDao;
    private UtilisateurDao utilisateurDao;
    private JournalImportDao journalDao;
    private CsvImportServiceImpl service;
    private final List<Client> enregistres = new ArrayList<>();

    @BeforeEach
    void preparer() {
        clientDao = mock(ClientDao.class);
        commercialDao = mock(CommercialDao.class);
        utilisateurDao = mock(UtilisateurDao.class);
        journalDao = mock(JournalImportDao.class);

        when(clientDao.save(any(Client.class))).thenAnswer(appel -> {
            Client client = appel.getArgument(0);
            enregistres.add(client);
            return client;
        });
        when(clientDao.existsByCode("EXISTANT")).thenReturn(true);
        when(utilisateurDao.findByEmail(any())).thenReturn(Optional.empty());
        when(journalDao.save(any(JournalImport.class))).thenAnswer(appel -> {
            JournalImport journal = appel.getArgument(0);
            journal.setId(7L);
            return journal;
        });

        service = new CsvImportServiceImpl(clientDao, commercialDao, utilisateurDao, journalDao);
    }

    @Test
    void importeLesLignesValidesEtRejetteLesAutresAvecLeurMotif() {
        String csv = ENTETE
                + "CLI-1;Atlas SARL;001234567000089;ENTREPRISE;contact@atlas.ma;0522000000;Rue 1;Casablanca;Commerce;45;ACTIF;\n"
                + "CLI-2;Sans type;;;;;;;;;;\n"
                + "CLI-3;Mauvais mail;;ENTREPRISE;pas-un-mail;;;;;;;\n"
                + "CLI-1;Doublon dans le fichier;;ENTREPRISE;;;;;;;;\n"
                + "EXISTANT;Déjà en base;;ENTREPRISE;;;;;;;;\n"
                + "CLI-4;ICE trop court;123;ENTREPRISE;;;;;;;;\n"
                + "CLI-5;Délai absurde;;PARTICULIER;;;;;;999;;\n";

        RapportImportDto rapport = importer(csv);

        assertThat(rapport.lignesLues()).isEqualTo(7);
        assertThat(rapport.lignesImportees()).isEqualTo(1);
        assertThat(rapport.lignesRejetees()).isEqualTo(6);
        assertThat(rapport.rejets()).extracting(r -> r.numeroLigne()).containsExactly(3, 4, 5, 6, 7, 8);
        assertThat(rapport.rejets().get(0).motif()).contains("type");
        assertThat(rapport.rejets().get(1).motif()).contains("e-mail");
        assertThat(rapport.rejets().get(2).motif()).contains("doublon");
        assertThat(rapport.rejets().get(3).motif()).contains("existe déjà");
        assertThat(rapport.rejets().get(4).motif()).contains("ICE");
        assertThat(rapport.rejets().get(5).motif()).contains("180");

        assertThat(enregistres).hasSize(1);
        Client client = enregistres.get(0);
        assertThat(client.getCode()).isEqualTo("CLI-1");
        assertThat(client.getDelaiPaiementJours()).isEqualTo(45);
        assertThat(client.getType()).isEqualTo(TypeClient.ENTREPRISE);
        assertThat(client.getStatut()).isEqualTo(StatutClient.ACTIF);
    }

    @Test
    void nettoieLesValeursAvantEnregistrement() {
        String csv = ENTETE
                + "  cli-9 ;  Atlas   Négoce  ;001 234 567 000 089;entreprise; Contact@ATLAS.MA ;+212 5.22-00 00 00;;  Casablanca ;;;prospect;\n";

        RapportImportDto rapport = importer(csv);

        assertThat(rapport.lignesImportees()).isEqualTo(1);
        Client client = enregistres.get(0);
        assertThat(client.getCode()).isEqualTo("CLI-9");
        assertThat(client.getRaisonSociale()).isEqualTo("Atlas Négoce");
        assertThat(client.getIce()).isEqualTo("001234567000089");
        assertThat(client.getEmail()).isEqualTo("contact@atlas.ma");
        assertThat(client.getTelephone()).isEqualTo("+212522000000");
        assertThat(client.getVille()).isEqualTo("Casablanca");
        assertThat(client.getAdresse()).isNull();
        assertThat(client.getStatut()).isEqualTo(StatutClient.PROSPECT);
        assertThat(client.getDelaiPaiementJours()).isEqualTo(30);   // valeur par défaut
    }

    @Test
    void accepteLaVirguleCommeSeparateurEtLeBomExcel() {
        String csv = "\uFEFFcode,raison_sociale,type\nCLI-1,Atlas,ENTREPRISE\nCLI-2,\"Dupont, Jean\",PARTICULIER\n";

        RapportImportDto rapport = importer(csv);

        assertThat(rapport.lignesImportees()).isEqualTo(2);
        assertThat(enregistres.get(1).getRaisonSociale()).isEqualTo("Dupont, Jean");
        assertThat(enregistres.get(1).getType()).isEqualTo(TypeClient.PARTICULIER);
    }

    @Test
    void litUnFichierWindows1252ExporteParExcel() {
        String csv = "code;raison_sociale;type\nCLI-1;Société Générale Maroc;ENTREPRISE\n";
        byte[] octets = csv.getBytes(Charset.forName("windows-1252"));

        RapportImportDto rapport = service.importerClients("export-excel.csv", new ByteArrayInputStream(octets));

        assertThat(rapport.lignesImportees()).isEqualTo(1);
        assertThat(enregistres.get(0).getRaisonSociale()).isEqualTo("Société Générale Maroc");
    }

    @Test
    void rejetteUnCommercialInconnu() {
        String csv = ENTETE + "CLI-1;Atlas;;ENTREPRISE;;;;;;;;inconnu@clientpulse.ma\n";

        RapportImportDto rapport = importer(csv);

        assertThat(rapport.lignesImportees()).isZero();
        assertThat(rapport.rejets().get(0).motif()).contains("commercial");
    }

    @Test
    void ignoreLesLignesVidesEtJournaliseLImport() {
        String csv = ENTETE + "CLI-1;Atlas;;ENTREPRISE;;;;;;;;\n;;;;;;;;;;;\n\n";

        RapportImportDto rapport = service.importerClients("C:\\docs\\clients.csv",
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertThat(rapport.lignesLues()).isEqualTo(1);
        ArgumentCaptor<JournalImport> capture = ArgumentCaptor.forClass(JournalImport.class);
        org.mockito.Mockito.verify(journalDao).save(capture.capture());
        JournalImport journal = capture.getValue();
        assertThat(journal.getFichier()).isEqualTo("clients.csv");   // chemin retiré
        assertThat(journal.getLignesLues()).isEqualTo(1);
        assertThat(journal.getLignesImportees()).isEqualTo(1);
        assertThat(journal.getLignesRejetees()).isZero();
        assertThat(rapport.journalId()).isEqualTo(7L);
    }

    @Test
    void refuseUnFichierSansColonneObligatoire() {
        assertThatThrownBy(() -> importer("code;nom_complet\nCLI-1;Atlas\n"))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("type");
    }

    @Test
    void refuseUnFichierVideOuSansDonnees() {
        assertThatThrownBy(() -> importer("")).isInstanceOf(RegleMetierException.class);
        assertThatThrownBy(() -> importer("code;raison_sociale;type\n"))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("aucune ligne");
    }

    @Test
    void proposeUnModeleCsvAvecLesColonnesAttendues() {
        assertThat(service.modeleClients()).startsWith("code;raison_sociale;ice;type;");
    }

    private RapportImportDto importer(String csv) {
        return service.importerClients("clients.csv", new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }
}

package ma.ensam.clientpulse.service.impl;

import com.lowagie.text.pdf.PdfReader;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.enums.ModePaiement;
import ma.ensam.clientpulse.domain.enums.StatutDevis;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeClient;
import ma.ensam.clientpulse.domain.enums.TypeFacture;
import ma.ensam.clientpulse.dto.DevisDto;
import ma.ensam.clientpulse.dto.DocumentPdfDto;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.LigneDto;
import ma.ensam.clientpulse.dto.PaiementDto;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.DevisService;
import ma.ensam.clientpulse.service.interfaces.FacturationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfServiceImplTest {

    private DevisService devisService;
    private FacturationService facturationService;
    private ClientDao clientDao;
    private PdfServiceImpl service;

    @BeforeEach
    void preparer() {
        devisService = mock(DevisService.class);
        facturationService = mock(FacturationService.class);
        clientDao = mock(ClientDao.class);

        Client client = new Client();
        client.setId(5L);
        client.setCode("CLI-0005");
        client.setRaisonSociale("Société Générale Maroc");
        client.setIce("001234567000089");
        client.setAdresse("55 boulevard Zerktouni");
        client.setVille("Casablanca");
        client.setEmail("compta@sgmaroc.ma");
        client.setType(TypeClient.ENTREPRISE);
        client.setDelaiPaiementJours(45);
        when(clientDao.findById(5L)).thenReturn(Optional.of(client));

        service = new PdfServiceImpl(devisService, facturationService, clientDao,
                "ClientPulse SARL", "Casablanca, Maroc", "000000000000000", "+212 5 22 00 00 00", "contact@clientpulse.ma");
    }

    @Test
    void genereUneFacturePdfValideAvecSesPaiements() throws IOException {
        when(facturationService.trouver(10L)).thenReturn(facture(TypeFacture.FACTURE, StatutFacture.PARTIELLEMENT_PAYEE, "FAC-2026-0001"));

        DocumentPdfDto document = service.genererFacture(10L);

        assertThat(document.nomFichier()).isEqualTo("facture-FAC-2026-0001.pdf");
        assertThat(new String(document.contenu(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(pages(document)).isEqualTo(1);
    }

    @Test
    void unBrouillonN_ADePasDeNumeroDansLeNomDuFichier() {
        when(facturationService.trouver(11L)).thenReturn(facture(TypeFacture.FACTURE, StatutFacture.BROUILLON, null));

        assertThat(service.genererFacture(11L).nomFichier()).isEqualTo("facture-brouillon.pdf");
    }

    @Test
    void unAvoirPorteSonPropreNom() {
        when(facturationService.trouver(12L)).thenReturn(facture(TypeFacture.AVOIR, StatutFacture.EMISE, "AV-2026-0001"));

        assertThat(service.genererFacture(12L).nomFichier()).isEqualTo("avoir-AV-2026-0001.pdf");
    }

    @Test
    void genereUnDevisPdfValide() throws IOException {
        when(devisService.trouver(3L)).thenReturn(new DevisDto(3L, "DEV-2026-0003", LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 12, 10), StatutDevis.ENVOYE, 5L, null,
                List.of(ligne("Licence annuelle", "2", "1000.00", "10", "20", "1800.00", "360.00")),
                new BigDecimal("1800.00"), new BigDecimal("360.00"), new BigDecimal("2160.00")));

        DocumentPdfDto document = service.genererDevis(3L);

        assertThat(document.nomFichier()).isEqualTo("devis-DEV-2026-0003.pdf");
        assertThat(pages(document)).isEqualTo(1);
    }

    @Test
    void unDocumentLongTientSurPlusieursPagesSansErreur() throws IOException {
        List<LigneDto> lignes = new java.util.ArrayList<>();
        for (int i = 1; i <= 80; i++) {
            lignes.add(ligne("Prestation " + i, "1", "100.00", "0", "20", "100.00", "20.00"));
        }
        when(devisService.trouver(4L)).thenReturn(new DevisDto(4L, "DEV-2026-0004", LocalDate.of(2026, 11, 10), null,
                StatutDevis.BROUILLON, 5L, null, lignes,
                new BigDecimal("8000.00"), new BigDecimal("1600.00"), new BigDecimal("9600.00")));

        assertThat(pages(service.genererDevis(4L))).isGreaterThan(1);
    }

    @Test
    void unClientInconnuDonneUneErreurMetierClaire() {
        when(devisService.trouver(9L)).thenReturn(new DevisDto(9L, "DEV-X", null, null, StatutDevis.BROUILLON, 404L, null,
                List.of(ligne("X", "1", "1.00", "0", "0", "1.00", "0.00")),
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE));
        when(clientDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.genererDevis(9L)).isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void regroupeLaTvaParTauxEtRecalculeLesMontantsAbsents() {
        List<LigneDto> lignes = List.of(
                ligne("A", "1", "100.00", "0", "20", "100.00", "20.00"),
                ligne("B", "2", "50.00", "0", "20", "100.00", "20.00"),
                ligne("C", "1", "200.00", "50", "10", null, null));   // brouillon : pas de montants stockés

        Map<BigDecimal, BigDecimal> tva = PdfServiceImpl.tvaParTaux(lignes);

        assertThat(tva).hasSize(2);
        assertThat(tva.get(new BigDecimal("20.00"))).isEqualByComparingTo("40.00");
        assertThat(tva.get(new BigDecimal("10.00"))).isEqualByComparingTo("10.00");   // 200 x 50 % = 100 HT, 10 % = 10
        assertThat(PdfServiceImpl.montantHt(lignes.get(2))).isEqualByComparingTo("100.00");
    }

    // ------------------------------------------------------------------ données

    private static int pages(DocumentPdfDto document) throws IOException {
        PdfReader lecteur = new PdfReader(document.contenu());
        try {
            return lecteur.getNumberOfPages();
        } finally {
            lecteur.close();
        }
    }

    private static LigneDto ligne(String designation, String quantite, String prix, String remise, String tva,
                                  String ht, String montantTva) {
        return new LigneDto(null, null, designation, new BigDecimal(quantite), new BigDecimal(prix),
                new BigDecimal(remise), new BigDecimal(tva),
                ht == null ? null : new BigDecimal(ht), montantTva == null ? null : new BigDecimal(montantTva));
    }

    private static FactureDto facture(TypeFacture type, StatutFacture statut, String numero) {
        return new FactureDto(10L, numero, type, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 16), statut, 5L, null, null,
                List.of(ligne("Abonnement CRM", "3", "500.00", "0", "20", "1500.00", "300.00"),
                        ligne("Formation", "1", "800.00", "0", "10", "800.00", "80.00")),
                List.of(new PaiementDto(1L, 10L, LocalDate.of(2026, 11, 15), new BigDecimal("1000.00"), ModePaiement.VIREMENT, "VIR-001")),
                new BigDecimal("2300.00"), new BigDecimal("380.00"), new BigDecimal("2680.00"),
                new BigDecimal("1000.00"), new BigDecimal("1680.00"));
    }
}

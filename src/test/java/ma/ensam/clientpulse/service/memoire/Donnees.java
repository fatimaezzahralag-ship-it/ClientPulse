package ma.ensam.clientpulse.service.memoire;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.LigneFacture;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Petites fabriques d'objets pour ne pas répéter le même code dans chaque test. */
public final class Donnees {

    private Donnees() {
    }

    public static Client client(long id) {
        Client c = new Client();
        c.setId(id);
        return c;
    }

    /** Une facture EMISE d'une seule ligne : prix HT, TVA 20 %. Exemple : ttc("1000") donne 1200 TTC. */
    public static Facture factureEmise(Client client, String prixHT, LocalDate echeance, String numero) {
        LigneFacture ligne = new LigneFacture();
        ligne.setDesignation("Prestation");
        ligne.setQuantite(BigDecimal.ONE);
        ligne.setPrixUnitaireHT(new BigDecimal(prixHT));
        ligne.setRemise(BigDecimal.ZERO);
        ligne.setTauxTVA(new BigDecimal("20.00"));

        BigDecimal ht = new BigDecimal(prixHT);
        BigDecimal tva = ht.multiply(new BigDecimal("0.20"));

        Facture f = new Facture();
        f.setClient(client);
        f.ajouterLigne(ligne);
        f.setTotalHT(ht);
        f.setTotalTVA(tva);
        f.setTotalTTC(ht.add(tva));
        f.setDateEmission(echeance.minusDays(30));
        f.setDateEcheance(echeance);
        f.emettre(numero);
        return f;
    }
}

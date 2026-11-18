package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.domain.entity.LigneCalculable;
import ma.ensam.clientpulse.dto.Totaux;
import ma.ensam.clientpulse.service.interfaces.CalculateurMontants;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CalculateurMontantsImpl implements CalculateurMontants {

    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    @Override
    public BigDecimal montantHT(LigneCalculable ligne) {
        BigDecimal quantite = exiger(ligne.getQuantite(), "La quantité");
        BigDecimal prix = exiger(ligne.getPrixUnitaireHT(), "Le prix unitaire HT");
        BigDecimal remise = valeurOuZero(ligne.getRemise());

        BigDecimal facteur = BigDecimal.ONE.subtract(remise.divide(CENT)); // remise 10 -> facteur 0.90
        return quantite.multiply(prix).multiply(facteur).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal montantTVA(LigneCalculable ligne) {
        BigDecimal taux = valeurOuZero(ligne.getTauxTVA());
        return montantHT(ligne).multiply(taux).divide(CENT, 2, RoundingMode.HALF_UP);
    }

    @Override
    public Totaux calculer(List<? extends LigneCalculable> lignes) {
        BigDecimal totalHT = BigDecimal.ZERO.setScale(2);
        BigDecimal totalTVA = BigDecimal.ZERO.setScale(2);
        Map<BigDecimal, BigDecimal> tvaParTaux = new TreeMap<>(); // trié par taux croissant

        for (LigneCalculable ligne : lignes) {
            BigDecimal ht = montantHT(ligne);
            BigDecimal tva = montantTVA(ligne);
            totalHT = totalHT.add(ht);
            totalTVA = totalTVA.add(tva);
            tvaParTaux.merge(valeurOuZero(ligne.getTauxTVA()), tva, BigDecimal::add);
        }
        return new Totaux(totalHT, totalTVA, totalHT.add(totalTVA), tvaParTaux);
    }

    private BigDecimal exiger(BigDecimal valeur, String nom) {
        if (valeur == null) {
            throw new IllegalArgumentException(nom + " est obligatoire.");
        }
        return valeur;
    }

    private BigDecimal valeurOuZero(BigDecimal valeur) {
        return valeur == null ? BigDecimal.ZERO : valeur;
    }
}

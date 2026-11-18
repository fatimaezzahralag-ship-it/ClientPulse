package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.entity.LigneCalculable;
import ma.ensam.clientpulse.dto.Totaux;

import java.math.BigDecimal;
import java.util.List;

/**
 * SOURCE UNIQUE des calculs HT / TVA / TTC. Devis, factures et PDF passent tous par ici :
 * la règle n'est écrite qu'une fois, donc elle ne peut pas être différente selon l'écran.
 */
public interface CalculateurMontants {

    /** Montant HT d'une ligne = quantité x prix x (1 - remise %), arrondi à 2 décimales. */
    BigDecimal montantHT(LigneCalculable ligne);

    /** Montant de TVA d'une ligne = montant HT x taux %, arrondi à 2 décimales. */
    BigDecimal montantTVA(LigneCalculable ligne);

    /** Totaux d'une liste de lignes (LigneDevis ou LigneFacture). */
    Totaux calculer(List<? extends LigneCalculable> lignes);
}

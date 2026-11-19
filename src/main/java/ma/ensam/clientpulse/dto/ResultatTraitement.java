package ma.ensam.clientpulse.dto;

/** Bilan du traitement de nuit : combien de factures sont passées en retard, combien de relances créées. */
public record ResultatTraitement(int facturesPasseesEnRetard, int relancesCreees) {
}

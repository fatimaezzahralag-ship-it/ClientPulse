package ma.ensam.clientpulse.dto;

/** Relances terminées sur la période et nombre de celles suivies d'un paiement ou d'une facture. */
public record SuccesRelancesDto(long effectuees, long reussies) {
}

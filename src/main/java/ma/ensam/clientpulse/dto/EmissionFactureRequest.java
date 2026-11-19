package ma.ensam.clientpulse.dto;

import java.time.LocalDate;

/**
 * Corps (facultatif) de l'émission d'une facture. Les deux dates sont facultatives :
 * dateEmission vaut aujourd'hui par défaut, dateEcheance vaut dateEmission + 30 jours par défaut.
 */
public record EmissionFactureRequest(LocalDate dateEmission, LocalDate dateEcheance) {
}

package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.NotNull;

import ma.ensam.clientpulse.domain.enums.NatureRelance;

import java.time.LocalDate;

/**
 * Contrat commun de création d'une relance.
 *
 * P1 utilise nature PAIEMENT, P2 utilise nature COMMERCIALE.
 * Le commercial est facultatif : le service déduit le responsable du client
 * lorsqu'il n'est pas fourni explicitement.
 */
public record RelanceRequest(

        @NotNull Long clientId,

        Long commercialId,

        @NotNull NatureRelance nature,

        String motif,

        LocalDate dateEcheance

) {}

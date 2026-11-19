package ma.ensam.clientpulse.dto;

import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Score de risque exposé par l'API.
 *
 * {@code facteursExplicatifs} est une liste : en base les explications sont
 * stockées sur plusieurs lignes dans une colonne TEXT.
 */
public record ScoreClientDto(

        Long id,

        Long clientId,

        LocalDate dateCalcul,

        BigDecimal scoreRisque,

        NiveauRisque niveau,

        SegmentRFM segment,

        List<String> facteursExplicatifs

) {}

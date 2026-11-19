package ma.ensam.clientpulse.dto;

import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;

import java.math.BigDecimal;
import java.util.List;

/** Client à risque affiché dans le tableau de bord et la liste des relances. */
public record ClientARelancerDto(

        Long clientId,

        String code,

        String raisonSociale,

        BigDecimal scoreRisque,

        NiveauRisque niveau,

        SegmentRFM segment,

        List<String> facteursExplicatifs

) {}

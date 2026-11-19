package ma.ensam.clientpulse.dto;

import java.util.Map;

/** Indicateurs agrégés du tableau de bord analytique. */
public record AnalyticsDashboardDto(

        long clientsEvalues,

        long clientsARisque,

        Map<String, Long> repartitionNiveaux,

        Map<String, Long> repartitionSegments

) {}

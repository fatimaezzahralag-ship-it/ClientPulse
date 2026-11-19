package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.dto.AnalyticsDashboardDto;
import ma.ensam.clientpulse.dto.ClientARelancerDto;
import ma.ensam.clientpulse.dto.ScoreClientDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/** Lectures analytiques exposées sous /api/analytics. */
public interface AnalyticsService {

    Page<ScoreClientDto> scores(NiveauRisque niveau, Pageable pageable);

    Map<String, Long> repartitionSegments();

    Map<String, Long> repartitionNiveaux();

    Page<ClientARelancerDto> clientsARelancer(
            NiveauRisque niveau, Pageable pageable);

    ScoreClientDto scoreClient(Long clientId);

    AnalyticsDashboardDto dashboard();

    /** Déclenche un recalcul manuel ; false si un recalcul est déjà en cours. */
    boolean declencherRecalcul();
}

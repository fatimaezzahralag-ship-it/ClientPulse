package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;
import ma.ensam.clientpulse.dto.AnalyticsDashboardDto;
import ma.ensam.clientpulse.dto.ClientARelancerDto;
import ma.ensam.clientpulse.dto.ScoreClientDto;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.AnalyticsService;
import ma.ensam.clientpulse.service.interfaces.ChurnScoringService;
import ma.ensam.clientpulse.service.job.ScoringJob;
import ma.ensam.clientpulse.service.mapper.ScoreClientMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Agrège les scores sans jamais recalculer de valeurs dans le contrôleur. */
@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ScoreClientDao scoreClientDao;
    private final ScoreClientMapper scoreClientMapper;
    private final ChurnScoringService churnScoringService;
    private final ScoringJob scoringJob;

    public AnalyticsServiceImpl(
            ScoreClientDao scoreClientDao,
            ScoreClientMapper scoreClientMapper,
            ChurnScoringService churnScoringService,
            ScoringJob scoringJob) {
        this.scoreClientDao = scoreClientDao;
        this.scoreClientMapper = scoreClientMapper;
        this.churnScoringService = churnScoringService;
        this.scoringJob = scoringJob;
    }

    @Override
    public Page<ScoreClientDto> scores(
            NiveauRisque niveau, Pageable pageable) {
        return (niveau == null
                ? scoreClientDao.findDerniersScores(pageable)
                : scoreClientDao.findDerniersScoresByNiveau(niveau, pageable))
                .map(scoreClientMapper::toDto);
    }

    @Override
    public Map<String, Long> repartitionSegments() {
        Map<String, Long> resultat = new LinkedHashMap<>();
        for (SegmentRFM segment : SegmentRFM.values()) {
            resultat.put(segment.name(), 0L);
        }
        scoreClientDao.findDerniersScores().stream()
                .filter(score -> score.getSegment() != null)
                .collect(Collectors.groupingBy(
                        score -> score.getSegment().name(),
                        Collectors.counting()))
                .forEach(resultat::put);
        return resultat;
    }

    @Override
    public Map<String, Long> repartitionNiveaux() {
        Map<String, Long> resultat = new LinkedHashMap<>();
        for (NiveauRisque niveau : NiveauRisque.values()) {
            resultat.put(niveau.name(), 0L);
        }
        scoreClientDao.findDerniersScores().stream()
                .filter(score -> score.getNiveau() != null)
                .collect(Collectors.groupingBy(
                        score -> score.getNiveau().name(),
                        Collectors.counting()))
                .forEach(resultat::put);
        return resultat;
    }

    @Override
    public Page<ClientARelancerDto> clientsARelancer(
            NiveauRisque niveau, Pageable pageable) {
        return scoreClientDao.findDerniersScoresARelancer(niveau, pageable)
                .map(this::versClientARelancer);
    }

    @Override
    public ScoreClientDto scoreClient(Long clientId) {
        return churnScoringService.dernierScore(clientId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun score disponible pour le client : " + clientId));
    }

    @Override
    public AnalyticsDashboardDto dashboard() {
        List<ScoreClient> scores = scoreClientDao.findDerniersScores();
        long aRelancer = scores.stream()
                .filter(score -> score.getNiveau() == NiveauRisque.ELEVE
                        || score.getNiveau() == NiveauRisque.CRITIQUE)
                .count();

        return new AnalyticsDashboardDto(
                scores.size(),
                aRelancer,
                repartitionNiveaux(),
                repartitionSegments()
        );
    }

    @Override
    @Transactional(readOnly = false)
    public boolean declencherRecalcul() {
        return scoringJob.executer();
    }

    private ClientARelancerDto versClientARelancer(ScoreClient score) {
        var client = score.getClient();
        return new ClientARelancerDto(
                client != null ? client.getId() : null,
                client != null ? client.getCode() : null,
                client != null ? client.getRaisonSociale() : null,
                score.getScoreRisque(),
                score.getNiveau(),
                score.getSegment(),
                scoreClientMapper.toDto(score).facteursExplicatifs()
        );
    }
}

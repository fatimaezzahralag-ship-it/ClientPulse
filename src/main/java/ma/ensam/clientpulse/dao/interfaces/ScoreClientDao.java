package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ScoreClientDao {

    ScoreClient save(ScoreClient score);

    Optional<ScoreClient> findDernierByClientId(Long clientId);

    List<ScoreClient> findHistoriqueByClientId(Long clientId);

    /** Dernier score de chaque client, utile aux tableaux de bord. */
    List<ScoreClient> findDerniersScores();

    Page<ScoreClient> findDerniersScores(Pageable pageable);

    Page<ScoreClient> findDerniersScoresByNiveau(
            NiveauRisque niveau, Pageable pageable);

    /** Clients à relancer : derniers scores de niveau élevé ou critique. */
    List<ScoreClient> findDerniersScoresARelancer();

    Page<ScoreClient> findDerniersScoresARelancer(
            NiveauRisque niveau, Pageable pageable);
}

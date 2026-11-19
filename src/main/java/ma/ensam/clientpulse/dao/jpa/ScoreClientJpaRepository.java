package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScoreClientJpaRepository
        extends JpaRepository<ScoreClient, Long> {

    Optional<ScoreClient> findFirstByClientIdOrderByDateCalculDescIdDesc(
            Long clientId);

    List<ScoreClient> findByClientIdOrderByDateCalculDescIdDesc(
            Long clientId);

    /**
     * Dernier score de chaque client. La sous-requête sur l'identifiant évite
     * les doublons lorsque deux scores ont la même date de calcul.
     */
    @Query("""
            SELECT s FROM ScoreClient s
            WHERE s.id = (
                SELECT MAX(s2.id) FROM ScoreClient s2
                WHERE s2.client.id = s.client.id
            )
            """)
    List<ScoreClient> findDerniersScores();

    @Query("""
            SELECT s FROM ScoreClient s
            WHERE s.id = (
                SELECT MAX(s2.id) FROM ScoreClient s2
                WHERE s2.client.id = s.client.id
            )
            """)
    Page<ScoreClient> findDerniersScores(Pageable pageable);

    @Query("""
            SELECT s FROM ScoreClient s
            WHERE (:niveau IS NULL OR s.niveau = :niveau)
              AND s.id = (
                SELECT MAX(s2.id) FROM ScoreClient s2
                WHERE s2.client.id = s.client.id
              )
            """)
    Page<ScoreClient> findDerniersScoresByNiveau(
            @Param("niveau") NiveauRisque niveau,
            Pageable pageable);

    @Query("""
            SELECT s FROM ScoreClient s
            WHERE s.niveau IN :niveaux
              AND s.id = (
                SELECT MAX(s2.id) FROM ScoreClient s2
                WHERE s2.client.id = s.client.id
              )
            ORDER BY s.scoreRisque DESC, s.id DESC
            """)
    List<ScoreClient> findDerniersScoresByNiveaux(
            @Param("niveaux") List<NiveauRisque> niveaux);

    @Query("""
            SELECT s FROM ScoreClient s
            WHERE s.niveau IN :niveaux
              AND s.id = (
                SELECT MAX(s2.id) FROM ScoreClient s2
                WHERE s2.client.id = s.client.id
              )
            ORDER BY s.scoreRisque DESC, s.id DESC
            """)
    Page<ScoreClient> findDerniersScoresByNiveaux(
            @Param("niveaux") List<NiveauRisque> niveaux,
            Pageable pageable);
}

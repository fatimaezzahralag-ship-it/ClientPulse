package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.ScoreClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoreClientJpaRepository
        extends JpaRepository<ScoreClient, Long> {

    Optional<ScoreClient> findFirstByClientIdOrderByDateCalculDescIdDesc(
            Long clientId);

    List<ScoreClient> findByClientIdOrderByDateCalculDescIdDesc(
            Long clientId);
}

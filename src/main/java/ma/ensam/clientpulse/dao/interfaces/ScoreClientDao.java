package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.ScoreClient;

import java.util.List;
import java.util.Optional;

public interface ScoreClientDao {

    ScoreClient save(ScoreClient score);

    Optional<ScoreClient> findDernierByClientId(Long clientId);

    List<ScoreClient> findHistoriqueByClientId(Long clientId);
}

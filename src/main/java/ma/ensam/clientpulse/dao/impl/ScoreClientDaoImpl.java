package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.dao.jpa.ScoreClientJpaRepository;
import ma.ensam.clientpulse.domain.entity.ScoreClient;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ScoreClientDaoImpl implements ScoreClientDao {

    private final ScoreClientJpaRepository repository;

    public ScoreClientDaoImpl(ScoreClientJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ScoreClient save(ScoreClient score) {
        return repository.save(score);
    }

    @Override
    public Optional<ScoreClient> findDernierByClientId(Long clientId) {
        return repository
                .findFirstByClientIdOrderByDateCalculDescIdDesc(clientId);
    }

    @Override
    public List<ScoreClient> findHistoriqueByClientId(Long clientId) {
        return repository.findByClientIdOrderByDateCalculDescIdDesc(clientId);
    }
}

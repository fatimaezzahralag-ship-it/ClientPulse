package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.dao.jpa.ScoreClientJpaRepository;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ScoreClientDaoImpl implements ScoreClientDao {

    private static final List<NiveauRisque> NIVEAUX_A_RELANCER =
            List.of(NiveauRisque.ELEVE, NiveauRisque.CRITIQUE);

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

    @Override
    public List<ScoreClient> findDerniersScores() {
        return repository.findDerniersScores();
    }

    @Override
    public Page<ScoreClient> findDerniersScores(Pageable pageable) {
        return repository.findDerniersScores(pageable);
    }

    @Override
    public Page<ScoreClient> findDerniersScoresByNiveau(
            NiveauRisque niveau, Pageable pageable) {
        return repository.findDerniersScoresByNiveau(niveau, pageable);
    }

    @Override
    public List<ScoreClient> findDerniersScoresARelancer() {
        return repository.findDerniersScoresByNiveaux(NIVEAUX_A_RELANCER);
    }

    @Override
    public Page<ScoreClient> findDerniersScoresARelancer(
            NiveauRisque niveau, Pageable pageable) {
        List<NiveauRisque> niveaux = niveau == null
                ? NIVEAUX_A_RELANCER
                : List.of(niveau);
        return repository.findDerniersScoresByNiveaux(niveaux, pageable);
    }
}

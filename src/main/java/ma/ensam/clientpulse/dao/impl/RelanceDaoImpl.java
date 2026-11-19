package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.RelanceDao;
import ma.ensam.clientpulse.dao.jpa.RelanceJpaRepository;
import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RelanceDaoImpl implements RelanceDao {

    private static final List<StatutRelance> STATUTS_ACTIFS =
            List.of(StatutRelance.A_FAIRE, StatutRelance.EN_COURS);

    private final RelanceJpaRepository repository;

    public RelanceDaoImpl(RelanceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Relance save(Relance relance) {
        return repository.save(relance);
    }

    @Override
    public Optional<Relance> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Relance> findActiveByClientAndNature(
            Long clientId, NatureRelance nature) {
        return repository
                .findFirstByClientIdAndNatureAndStatutInOrderByIdDesc(
                        clientId, nature, STATUTS_ACTIFS);
    }

    @Override
    public Page<Relance> rechercher(
            Long commercialId, StatutRelance statut, Pageable pageable) {
        return repository.rechercher(commercialId, statut, pageable);
    }

    @Override
    public boolean existePourFactureEtNiveau(Long factureId, NatureRelance nature, int niveau) {
        return repository.existsByFactureIdAndNatureAndNiveau(factureId, nature, niveau);
    }
}

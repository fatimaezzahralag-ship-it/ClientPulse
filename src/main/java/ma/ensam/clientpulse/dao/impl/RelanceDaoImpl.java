package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.RelanceDao;
import ma.ensam.clientpulse.dao.jpa.RelanceJpaRepository;
import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RelanceDaoImpl implements RelanceDao {

    private final RelanceJpaRepository jpa;

    @Override
    public Relance save(Relance relance) {
        return jpa.save(relance);
    }

    @Override
    public Optional<Relance> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Relance> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Page<Relance> findByNature(NatureRelance nature, Pageable pageable) {
        return jpa.findByNature(nature, pageable);
    }

    @Override
    public boolean existePourFactureEtNiveau(Long factureId, NatureRelance nature, int niveau) {
        return jpa.existsByFactureIdAndNatureAndNiveau(factureId, nature, niveau);
    }
}

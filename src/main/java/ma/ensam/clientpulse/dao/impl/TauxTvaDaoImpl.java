package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.TauxTvaDao;
import ma.ensam.clientpulse.dao.jpa.TauxTvaJpaRepository;
import ma.ensam.clientpulse.domain.entity.TauxTVA;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TauxTvaDaoImpl implements TauxTvaDao {

    private final TauxTvaJpaRepository jpa;

    @Override
    public TauxTVA save(TauxTVA taux) {
        return jpa.save(taux);
    }

    @Override
    public Optional<TauxTVA> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<TauxTVA> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<TauxTVA> findByActif(boolean actif) {
        return jpa.findByActif(actif);
    }
}

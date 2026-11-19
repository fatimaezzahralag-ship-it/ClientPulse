package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.TauxTVA;

import java.util.List;
import java.util.Optional;

public interface TauxTvaDao {

    TauxTVA save(TauxTVA taux);

    Optional<TauxTVA> findById(Long id);

    List<TauxTVA> findAll();

    List<TauxTVA> findByActif(boolean actif);
}

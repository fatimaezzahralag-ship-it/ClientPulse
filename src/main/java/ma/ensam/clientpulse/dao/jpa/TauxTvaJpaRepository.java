package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.TauxTVA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TauxTvaJpaRepository extends JpaRepository<TauxTVA, Long> {

    List<TauxTVA> findByActif(boolean actif);
}

package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Devis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/** Contrat d'accès aux devis. La couche service ne connaît QUE cette interface (couplage faible). */
public interface DevisDao {

    Devis save(Devis devis);

    Optional<Devis> findById(Long id);

    Optional<Devis> findByNumero(String numero);

    List<Devis> findByClientId(Long clientId);

    List<Devis> findAll();

    Page<Devis> findAll(Pageable pageable);

    void deleteById(Long id);
}

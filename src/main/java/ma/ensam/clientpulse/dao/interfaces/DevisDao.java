package ma.ensam.clientpulse.dao.interfaces;
import ma.ensam.clientpulse.domain.entity.Devis;

import java.util.List;
import java.util.Optional;
public interface DevisDao {
    Devis save(Devis devis);
    Optional<Devis> findById(Long id);
    Optional<Devis> findByNumero(String numero);
    List<Devis> findByClientId(Long clientId);
    List<Devis> findAll();
    void deleteById(Long id);
}

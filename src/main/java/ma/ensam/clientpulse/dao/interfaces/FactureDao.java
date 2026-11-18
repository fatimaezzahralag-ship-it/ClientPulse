package ma.ensam.clientpulse.dao.interfaces;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.StatutFacture;

import java.util.List;
import java.util.Optional;
public interface FactureDao {
    Facture save(Facture facture);

    Optional<Facture> findById(Long id);

    Optional<Facture> findByNumero(String numero);

    List<Facture> findByClientId(Long clientId);

    List<Facture> findByStatut(StatutFacture statut);

    List<Facture> findAll();
}

package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Paiement;

import java.util.List;
import java.util.Optional;
public interface PaiementDao {
    Paiement save(Paiement paiement);

    Optional<Paiement> findById(Long id);

    List<Paiement> findByFactureId(Long factureId);
}

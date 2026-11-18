package ma.ensam.clientpulse.dao.jpa;
import ma.ensam.clientpulse.domain.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface PaiementJpaRepository extends JpaRepository<Paiement, Long>{
    List<Paiement> findByFactureId(Long factureId);
}

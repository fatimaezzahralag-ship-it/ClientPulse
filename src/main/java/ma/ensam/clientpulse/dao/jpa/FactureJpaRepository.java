package ma.ensam.clientpulse.dao.jpa;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface FactureJpaRepository extends JpaRepository<Facture , Long>{
    Optional<Facture> findByNumero(String numero);
    List<Facture> findByClientId(Long clientId);
    List<Facture> findByStatut(StatutFacture status);

}


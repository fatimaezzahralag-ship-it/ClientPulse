package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Devis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data fournit save, findById, findAll, deleteById... Les méthodes ci-dessous sont déduites de leur nom. */
public interface DevisJpaRepository extends JpaRepository<Devis, Long> {

    Optional<Devis> findByNumero(String numero);

    List<Devis> findByClientId(Long clientId);
}

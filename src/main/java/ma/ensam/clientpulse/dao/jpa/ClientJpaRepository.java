package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientJpaRepository
        extends JpaRepository<Client, Long>,
                JpaSpecificationExecutor<Client> {

    boolean existsByCode(String code);
}

package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InteractionJpaRepository
        extends JpaRepository<Interaction, Long>,
                JpaSpecificationExecutor<Interaction> {
}

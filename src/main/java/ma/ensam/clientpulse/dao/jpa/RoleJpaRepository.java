package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Role;
import ma.ensam.clientpulse.domain.enums.RoleNom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNom(RoleNom nom);

    boolean existsByNom(RoleNom nom);
}

package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Role;
import ma.ensam.clientpulse.domain.enums.RoleNom;

import java.util.Optional;

public interface RoleDao {

    Role save(Role role);

    Optional<Role> findById(Long id);

    Optional<Role> findByNom(RoleNom nom);

    boolean existsByNom(RoleNom nom);
}

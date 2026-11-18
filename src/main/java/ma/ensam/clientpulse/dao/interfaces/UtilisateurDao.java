package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface UtilisateurDao {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findById(Long id);

    List<Utilisateur> findAll();

    Utilisateur save(Utilisateur utilisateur);

    boolean existsByEmail(String email);
}

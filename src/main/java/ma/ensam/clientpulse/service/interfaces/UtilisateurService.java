package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.UtilisateurDto;
import ma.ensam.clientpulse.dto.UtilisateurModificationRequest;
import ma.ensam.clientpulse.dto.UtilisateurRequest;

import java.util.List;

public interface UtilisateurService {

    List<UtilisateurDto> findAll();

    UtilisateurDto findById(Long id);

    UtilisateurDto create(UtilisateurRequest request);

    UtilisateurDto modifier(Long id, UtilisateurModificationRequest request);

    /** Rôles attribuables depuis l'écran d'administration. */
    List<String> rolesDisponibles();

    void deactivate(Long id);
}

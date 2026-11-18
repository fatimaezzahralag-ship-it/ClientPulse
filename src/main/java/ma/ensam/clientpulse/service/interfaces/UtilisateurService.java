package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.UtilisateurDto;
import ma.ensam.clientpulse.dto.UtilisateurRequest;

import java.util.List;

public interface UtilisateurService {

    List<UtilisateurDto> findAll();

    UtilisateurDto findById(Long id);

    UtilisateurDto create(UtilisateurRequest request);

    void deactivate(Long id);
}

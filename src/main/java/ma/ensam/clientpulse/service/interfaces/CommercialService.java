package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.CommercialDto;

import java.util.List;

public interface CommercialService {

    List<CommercialDto> findAll();

    /** Identifiant du profil commercial associé à un email, null si aucun. */
    Long findIdByUtilisateurEmail(String email);
}

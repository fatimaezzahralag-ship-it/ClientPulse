package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.LoginRequest;
import ma.ensam.clientpulse.dto.UtilisateurDto;
import org.springframework.security.core.Authentication;

public interface AuthService {

    Authentication authenticate(LoginRequest request);

    UtilisateurDto getUtilisateurConnecte(String email);
}

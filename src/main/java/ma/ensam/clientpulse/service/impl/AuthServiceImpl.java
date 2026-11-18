package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.UtilisateurDao;
import ma.ensam.clientpulse.domain.entity.Utilisateur;
import ma.ensam.clientpulse.dto.LoginRequest;
import ma.ensam.clientpulse.dto.UtilisateurDto;
import ma.ensam.clientpulse.service.interfaces.AuthService;
import ma.ensam.clientpulse.service.mapper.UtilisateurMapper;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurDao utilisateurDao;
    private final UtilisateurMapper utilisateurMapper;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UtilisateurDao utilisateurDao,
            UtilisateurMapper utilisateurMapper) {
        this.authenticationManager = authenticationManager;
        this.utilisateurDao = utilisateurDao;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    public Authentication authenticate(LoginRequest request) {

        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.motDePasse()
            )
        );
    }

    @Override
    public UtilisateurDto getUtilisateurConnecte(String email) {

        Utilisateur utilisateur = utilisateurDao
            .findByEmail(email)
            .orElseThrow();

        return utilisateurMapper.toDto(utilisateur);
    }
}

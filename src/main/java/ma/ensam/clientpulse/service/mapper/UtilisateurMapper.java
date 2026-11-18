package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Utilisateur;
import ma.ensam.clientpulse.dto.UtilisateurDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UtilisateurMapper {

    public UtilisateurDto toDto(Utilisateur utilisateur) {

        return new UtilisateurDto(
            utilisateur.getId(),
            utilisateur.getNom(),
            utilisateur.getPrenom(),
            utilisateur.getEmail(),
            utilisateur.isActif(),
            utilisateur.getRoles()
                .stream()
                .map(role -> role.getNom().name())
                .collect(Collectors.toSet())
        );
    }
}

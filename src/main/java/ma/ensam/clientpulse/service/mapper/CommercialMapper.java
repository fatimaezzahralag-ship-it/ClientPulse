package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Commercial;
import ma.ensam.clientpulse.domain.entity.Utilisateur;
import ma.ensam.clientpulse.dto.CommercialDto;

import org.springframework.stereotype.Component;

@Component
public class CommercialMapper {

    public CommercialDto toDto(Commercial commercial) {

        Utilisateur utilisateur = commercial.getUtilisateur();

        return new CommercialDto(
                commercial.getId(),
                utilisateur != null ? utilisateur.getNom() : null,
                utilisateur != null ? utilisateur.getPrenom() : null,
                utilisateur != null ? utilisateur.getEmail() : null,
                commercial.getEquipe()
        );
    }
}

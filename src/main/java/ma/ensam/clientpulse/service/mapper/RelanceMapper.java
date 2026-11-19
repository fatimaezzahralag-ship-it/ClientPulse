package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.dto.RelanceDto;
import org.springframework.stereotype.Component;

@Component
public class RelanceMapper {

    public RelanceDto toDto(Relance relance) {

        String commercialNom = null;
        if (relance.getCommercial() != null
                && relance.getCommercial().getUtilisateur() != null) {
            var utilisateur = relance.getCommercial().getUtilisateur();
            commercialNom = (utilisateur.getPrenom() + " "
                    + utilisateur.getNom()).trim();
        }

        return new RelanceDto(
                relance.getId(),
                relance.getClient() != null ? relance.getClient().getId() : null,
                relance.getClient() != null ? relance.getClient().getCode() : null,
                relance.getClient() != null
                        ? relance.getClient().getRaisonSociale()
                        : null,
                relance.getCommercial() != null
                        ? relance.getCommercial().getId()
                        : null,
                commercialNom,
                relance.getNature(),
                relance.getStatut(),
                relance.getMotif(),
                relance.getResultat(),
                relance.getCommentaire(),
                relance.getDateEcheance(),
                relance.getDateCreation()
        );
    }
}

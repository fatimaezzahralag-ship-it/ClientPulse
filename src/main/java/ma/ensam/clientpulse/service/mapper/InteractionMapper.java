package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Interaction;
import ma.ensam.clientpulse.domain.enums.*;
import ma.ensam.clientpulse.dto.InteractionDto;
import ma.ensam.clientpulse.dto.InteractionRequest;
import org.springframework.stereotype.Component;

@Component
public class InteractionMapper {

    public Interaction toEntity(InteractionRequest request) {

        Interaction interaction = new Interaction();

        interaction.setType(request.type());
        interaction.setDate(request.date());
        interaction.setResume(request.resume());
        interaction.setStatut(request.statut());

        return interaction;
    }

    public InteractionDto toDto(Interaction interaction) {

        return new InteractionDto(
            interaction.getId(),
            interaction.getType().name(),
            interaction.getDate(),
            interaction.getResume(),
            interaction.getStatut() != null
                ? interaction.getStatut().name()
                : null,
            interaction.getClient() != null
                ? interaction.getClient().getId()
                : null
        );
    }
}

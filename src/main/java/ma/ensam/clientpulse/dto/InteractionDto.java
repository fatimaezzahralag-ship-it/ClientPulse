package ma.ensam.clientpulse.dto;

import java.time.LocalDateTime;

public record InteractionDto(
    Long id,
    String type,
    LocalDateTime date,
    String resume,
    String statut,
    Long clientId
) {}

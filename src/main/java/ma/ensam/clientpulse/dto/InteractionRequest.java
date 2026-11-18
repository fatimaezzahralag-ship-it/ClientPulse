package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ma.ensam.clientpulse.domain.enums.TypeInteraction;
import ma.ensam.clientpulse.domain.enums.StatutInteraction;

import java.time.LocalDateTime;

public record InteractionRequest(

    @NotNull
    TypeInteraction type,

    @NotNull
    LocalDateTime date,

    @NotBlank
    @Size(max = 2000)
    String resume,

    @NotNull
    StatutInteraction statut

) {}

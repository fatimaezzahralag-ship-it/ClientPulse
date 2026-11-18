package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.*;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;

public record ClientRequest(

    @NotBlank
    @Size(max = 30)
    String code,

    @NotBlank
    @Size(max = 150)
    String raisonSociale,

    @Size(max = 20)
    String ice,

    @NotNull
    TypeClient type,

    @Email
    @Size(max = 150)
    String email,

    @Size(max = 30)
    String telephone,

    @Size(max = 255)
    String adresse,

    @Size(max = 80)
    String ville,

    @Size(max = 80)
    String secteur,

    @NotNull
    @Min(0)
    @Max(180)
    Integer delaiPaiementJours,

    @NotNull
    StatutClient statut,

    Long commercialId

) {}

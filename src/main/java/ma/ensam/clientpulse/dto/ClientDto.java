package ma.ensam.clientpulse.dto;

import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;

public record ClientDto(

    Long id,
    String code,
    String raisonSociale,
    String ice,
    TypeClient type,
    String email,
    String telephone,
    String adresse,
    String ville,
    String secteur,
    Integer delaiPaiementJours,
    StatutClient statut,
    Long commercialId

) {}

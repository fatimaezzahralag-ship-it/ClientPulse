package ma.ensam.clientpulse.dto;

import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.ResultatRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Relance exposée à l'écran Angular (aucune entité JPA n'est sérialisée). */
public record RelanceDto(

        Long id,

        Long clientId,

        String codeClient,

        String raisonSociale,

        Long commercialId,

        String commercialNom,

        NatureRelance nature,

        StatutRelance statut,

        String motif,

        ResultatRelance resultat,

        String commentaire,

        LocalDate dateEcheance,

        LocalDateTime dateCreation

) {}

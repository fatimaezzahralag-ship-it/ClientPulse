package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.ResultatRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.RelanceDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

/** Transforme les scores de risque élevé en relances commerciales actionnables. */
public interface RelanceCommercialService {

    /** Génère les relances manquantes pour les clients à risque. */
    int genererRelances(LocalDate jour);

    Page<RelanceDto> rechercherRelances(
            Long commercialId, StatutRelance statut, Pageable pageable);

    Optional<RelanceDto> findById(Long relanceId);

    RelanceDto demarrerRelance(Long relanceId);

    void traiterRelance(Long relanceId, ResultatRelance resultat, String commentaire);
}

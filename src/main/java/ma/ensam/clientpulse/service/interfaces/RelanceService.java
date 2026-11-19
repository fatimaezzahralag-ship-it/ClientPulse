package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.ResultatRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.RelanceDto;
import ma.ensam.clientpulse.dto.RelanceRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Contrat commun des relances (P1 = PAIEMENT, P2 = COMMERCIALE).
 *
 * Ce contrat remplace celui que P1 devait fournir à J3 : il n'existait pas
 * dans le dépôt. L'entité unique est {@code Relance}.
 */
public interface RelanceService {

    /** Crée une relance, avec anti-doublon et affectation au commercial. */
    RelanceDto creer(RelanceRequest request);

    Optional<RelanceDto> relanceActive(Long clientId, NatureRelance nature);

    Optional<RelanceDto> findById(Long id);

    Page<RelanceDto> rechercher(
            Long commercialId, StatutRelance statut, Pageable pageable);

    /** Passe une relance de A_FAIRE à EN_COURS. */
    RelanceDto demarrer(Long relanceId);

    /** Termine la relance et enregistre son résultat. */
    RelanceDto traiter(
            Long relanceId, ResultatRelance resultat, String commentaire);
}

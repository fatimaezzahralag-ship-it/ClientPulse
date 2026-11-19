package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RelanceDao {

    Relance save(Relance relance);

    Optional<Relance> findById(Long id);

    /** Relance active (à faire ou en cours) d'un client pour une nature donnée. */
    Optional<Relance> findActiveByClientAndNature(
            Long clientId, NatureRelance nature);

    Page<Relance> rechercher(
            Long commercialId, StatutRelance statut, Pageable pageable);
}

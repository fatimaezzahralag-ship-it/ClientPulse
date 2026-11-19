package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RelanceDao {

    Relance save(Relance relance);

    Optional<Relance> findById(Long id);

    Page<Relance> findAll(Pageable pageable);

    Page<Relance> findByNature(NatureRelance nature, Pageable pageable);

    /** Sert à ne créer qu'une relance par niveau et par facture. */
    boolean existePourFactureEtNiveau(Long factureId, NatureRelance nature, int niveau);
}

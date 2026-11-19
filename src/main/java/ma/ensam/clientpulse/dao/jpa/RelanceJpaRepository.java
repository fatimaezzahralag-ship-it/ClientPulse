package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelanceJpaRepository extends JpaRepository<Relance, Long> {

    Page<Relance> findByNature(NatureRelance nature, Pageable pageable);

    boolean existsByFactureIdAndNatureAndNiveau(Long factureId, NatureRelance nature, int niveau);
}

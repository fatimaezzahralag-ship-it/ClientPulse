package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RelanceJpaRepository extends JpaRepository<Relance, Long> {

    Optional<Relance> findFirstByClientIdAndNatureAndStatutInOrderByIdDesc(
            Long clientId,
            NatureRelance nature,
            List<StatutRelance> statuts);

    @Query("""
            SELECT r FROM Relance r
            WHERE (:commercialId IS NULL OR r.commercial.id = :commercialId)
              AND (:statut IS NULL OR r.statut = :statut)
            ORDER BY r.id DESC
            """)
    Page<Relance> rechercher(
            @Param("commercialId") Long commercialId,
            @Param("statut") StatutRelance statut,
            Pageable pageable);

    boolean existsByFactureIdAndNatureAndNiveau(Long factureId, NatureRelance nature, Integer niveau);
}

package ma.ensam.clientpulse.dao.jpa;

import jakarta.persistence.LockModeType;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FactureJpaRepository extends JpaRepository<Facture, Long> {

    Optional<Facture> findByNumero(String numero);

    List<Facture> findByClientId(Long clientId);

    List<Facture> findByStatut(StatutFacture statut);

    Page<Facture> findByStatut(StatutFacture statut, Pageable pageable);

    boolean existsByDevisId(Long devisId);

    /**
     * Même chose que findById, mais la ligne est VERROUILLÉE jusqu'à la fin de la transaction.
     * Deux paiements simultanés sur la même facture passent donc l'un après l'autre.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Facture f where f.id = :id")
    Optional<Facture> chercherEtVerrouiller(@Param("id") Long id);

    /**
     * Les factures d'un type et de certains statuts, avec leurs paiements chargés d'un coup
     * (sinon une requête de plus par facture, au moment de calculer le reste à payer).
     */
    @Query("select distinct f from Facture f left join fetch f.paiements "
            + "where f.type = :type and f.statut in :statuts")
    List<Facture> chargerAvecPaiements(@Param("type") TypeFacture type,
                                       @Param("statuts") Collection<StatutFacture> statuts);
}

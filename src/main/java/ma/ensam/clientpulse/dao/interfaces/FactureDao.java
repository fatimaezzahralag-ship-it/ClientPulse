package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FactureDao {

    Facture save(Facture facture);

    Optional<Facture> findById(Long id);

    /** Comme findById, mais verrouille la facture jusqu'à la fin de la transaction (paiement, émission). */
    Optional<Facture> findByIdPourMiseAJour(Long id);

    Optional<Facture> findByNumero(String numero);

    List<Facture> findByClientId(Long clientId);

    List<Facture> findByStatut(StatutFacture statut);

    List<Facture> findAll();

    Page<Facture> findAll(Pageable pageable);

    Page<Facture> findByStatut(StatutFacture statut, Pageable pageable);

    /** Vrai si une facture existe déjà pour ce devis (une facture au plus par devis). */
    boolean existePourDevis(Long devisId);

    /**
     * Factures (hors avoirs) qui peuvent encore être payées : EMISE, PARTIELLEMENT_PAYEE, EN_RETARD.
     * Les paiements sont déjà chargés.
     */
    List<Facture> findOuvertes();
}

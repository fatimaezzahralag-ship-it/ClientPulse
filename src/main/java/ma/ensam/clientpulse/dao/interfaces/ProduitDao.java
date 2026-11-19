package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProduitDao {

    Produit save(Produit produit);

    Optional<Produit> findById(Long id);

    Optional<Produit> findByReference(String reference);

    Page<Produit> findAll(Pageable pageable);

    /** Cherche le mot dans la désignation ou la référence, sans tenir compte des majuscules. */
    Page<Produit> rechercher(String motCle, Pageable pageable);
}

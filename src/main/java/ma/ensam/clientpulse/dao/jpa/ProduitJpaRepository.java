package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProduitJpaRepository extends JpaRepository<Produit, Long> {

    Optional<Produit> findByReference(String reference);

    // Le nom de la méthode décrit la requête : (designation contient X) OU (reference contient X)
    Page<Produit> findByDesignationContainingIgnoreCaseOrReferenceContainingIgnoreCase(
            String designation, String reference, Pageable pageable);
}

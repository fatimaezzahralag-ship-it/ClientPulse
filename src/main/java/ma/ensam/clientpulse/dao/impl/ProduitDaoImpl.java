package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.ProduitDao;
import ma.ensam.clientpulse.dao.jpa.ProduitJpaRepository;
import ma.ensam.clientpulse.domain.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProduitDaoImpl implements ProduitDao {

    private final ProduitJpaRepository jpa;

    @Override
    public Produit save(Produit produit) {
        return jpa.save(produit);
    }

    @Override
    public Optional<Produit> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Produit> findByReference(String reference) {
        return jpa.findByReference(reference);
    }

    @Override
    public Page<Produit> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Page<Produit> rechercher(String motCle, Pageable pageable) {
        return jpa.findByDesignationContainingIgnoreCaseOrReferenceContainingIgnoreCase(motCle, motCle, pageable);
    }
}

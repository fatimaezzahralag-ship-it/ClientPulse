package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.dao.jpa.FactureJpaRepository;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FactureDaoImpl implements FactureDao {

    private final FactureJpaRepository jpa;

    @Override
    public Facture save(Facture facture) {
        return jpa.save(facture);
    }

    @Override
    public Optional<Facture> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Facture> findByIdPourMiseAJour(Long id) {
        return jpa.chercherEtVerrouiller(id);
    }

    @Override
    public Optional<Facture> findByNumero(String numero) {
        return jpa.findByNumero(numero);
    }

    @Override
    public List<Facture> findByClientId(Long clientId) {
        return jpa.findByClientId(clientId);
    }

    @Override
    public List<Facture> findByStatut(StatutFacture statut) {
        return jpa.findByStatut(statut);
    }

    @Override
    public List<Facture> findAll() {
        return jpa.findAll();
    }

    @Override
    public Page<Facture> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Page<Facture> findByStatut(StatutFacture statut, Pageable pageable) {
        return jpa.findByStatut(statut, pageable);
    }

    @Override
    public boolean existePourDevis(Long devisId) {
        return jpa.existsByDevisId(devisId);
    }

    @Override
    public List<Facture> findOuvertes() {
        return jpa.chargerAvecPaiements(TypeFacture.FACTURE, List.of(
                StatutFacture.EMISE, StatutFacture.PARTIELLEMENT_PAYEE, StatutFacture.EN_RETARD));
    }
}

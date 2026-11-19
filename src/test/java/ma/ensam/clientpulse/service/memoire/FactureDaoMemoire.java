package ma.ensam.clientpulse.service.memoire;

import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Faux DAO : garde les factures dans une liste Java, sans base de données.
 * C'est le COUPLAGE FAIBLE en action : FacturationServiceImpl ne connaît que l'interface FactureDao,
 * on peut donc lui donner cette version "en mémoire" pour tester la logique seule.
 */
public class FactureDaoMemoire implements FactureDao {

    private final List<Facture> factures = new ArrayList<>();
    private long prochainId = 1;

    @Override
    public Facture save(Facture facture) {
        if (facture.getId() == null) {
            facture.setId(prochainId++);
            factures.add(facture);
        }
        return facture;
    }

    @Override
    public Optional<Facture> findById(Long id) {
        return factures.stream().filter(f -> f.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Facture> findByIdPourMiseAJour(Long id) {
        return findById(id);
    }

    @Override
    public boolean existePourDevis(Long devisId) {
        return factures.stream().anyMatch(f -> f.getDevis() != null && f.getDevis().getId().equals(devisId));
    }

    @Override
    public List<Facture> findOuvertes() {
        return factures.stream()
                .filter(f -> f.getType() == TypeFacture.FACTURE)
                .filter(f -> f.getStatut() == StatutFacture.EMISE
                        || f.getStatut() == StatutFacture.PARTIELLEMENT_PAYEE
                        || f.getStatut() == StatutFacture.EN_RETARD)
                .toList();
    }

    // Les méthodes ci-dessous ne servent pas dans ces tests

    @Override
    public Optional<Facture> findByNumero(String numero) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Facture> findByClientId(Long clientId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Facture> findByStatut(StatutFacture statut) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Facture> findAll() {
        return List.copyOf(factures);
    }

    @Override
    public Page<Facture> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<Facture> findByStatut(StatutFacture statut, Pageable pageable) {
        throw new UnsupportedOperationException();
    }
}

package ma.ensam.clientpulse.service.memoire;

import ma.ensam.clientpulse.dao.interfaces.RelanceDao;
import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Faux DAO des relances, en mémoire (voir FactureDaoMemoire). */
public class RelanceDaoMemoire implements RelanceDao {

    private final List<Relance> relances = new ArrayList<>();
    private long prochainId = 1;

    /** Pour que les tests puissent regarder ce qui a été enregistré. */
    public List<Relance> toutes() {
        return List.copyOf(relances);
    }

    @Override
    public Relance save(Relance relance) {
        if (relance.getId() == null) {
            relance.setId(prochainId++);
            relances.add(relance);
        }
        return relance;
    }

    @Override
    public Optional<Relance> findById(Long id) {
        return relances.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Relance> findActiveByClientAndNature(Long clientId, NatureRelance nature) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<Relance> rechercher(Long commercialId, StatutRelance statut, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existePourFactureEtNiveau(Long factureId, NatureRelance nature, int niveau) {
        return relances.stream().anyMatch(r -> r.getFacture() != null
                && r.getFacture().getId().equals(factureId)
                && r.getNature() == nature
                && r.getNiveau() != null && r.getNiveau() == niveau);
    }
}

package ma.ensam.clientpulse.service.memoire;

import ma.ensam.clientpulse.dao.interfaces.DevisDao;
import ma.ensam.clientpulse.domain.entity.Devis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Faux DAO des devis, en mémoire (voir FactureDaoMemoire). */
public class DevisDaoMemoire implements DevisDao {

    private final List<Devis> devis = new ArrayList<>();
    private long prochainId = 1;

    @Override
    public Devis save(Devis d) {
        if (d.getId() == null) {
            d.setId(prochainId++);
            devis.add(d);
        }
        return d;
    }

    @Override
    public Optional<Devis> findById(Long id) {
        return devis.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    @Override
    public void deleteById(Long id) {
        devis.removeIf(d -> d.getId().equals(id));
    }

    @Override
    public Optional<Devis> findByNumero(String numero) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Devis> findByClientId(Long clientId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Devis> findAll() {
        return List.copyOf(devis);
    }

    @Override
    public Page<Devis> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }
}

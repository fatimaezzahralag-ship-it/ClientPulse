package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.PaiementDao;
import ma.ensam.clientpulse.dao.jpa.PaiementJpaRepository;
import ma.ensam.clientpulse.domain.entity.Paiement;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaiementDaoImpl implements PaiementDao {

    private final PaiementJpaRepository jpa;

    @Override
    public Paiement save(Paiement paiement) {
        return jpa.save(paiement);
    }

    @Override
    public Optional<Paiement> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<Paiement> findByFactureId(Long factureId) {
        return jpa.findByFactureId(factureId);
    }
}

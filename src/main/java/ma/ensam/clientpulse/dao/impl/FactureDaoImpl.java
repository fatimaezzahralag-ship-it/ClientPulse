package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.dao.jpa.FactureJpaRepository;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
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
}

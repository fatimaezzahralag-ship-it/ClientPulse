package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.DevisDao;
import ma.ensam.clientpulse.dao.jpa.DevisJpaRepository;
import ma.ensam.clientpulse.domain.entity.Devis;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation de DevisDao. Elle ne fait que passer la main au repository Spring Data.
 * @RequiredArgsConstructor (Lombok) crée le constructeur : Spring injecte le repository tout seul (IoC).
 */
@Repository
@RequiredArgsConstructor
public class DevisDaoImpl implements DevisDao {

    private final DevisJpaRepository jpa;

    @Override
    public Devis save(Devis devis) {
        return jpa.save(devis);
    }

    @Override
    public Optional<Devis> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Devis> findByNumero(String numero) {
        return jpa.findByNumero(numero);
    }

    @Override
    public List<Devis> findByClientId(Long clientId) {
        return jpa.findByClientId(clientId);
    }

    @Override
    public List<Devis> findAll() {
        return jpa.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}

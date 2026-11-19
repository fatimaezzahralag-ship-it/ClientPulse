package ma.ensam.clientpulse.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ma.ensam.clientpulse.dao.interfaces.ReferencesExternesDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Commercial;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** EntityManager = l'outil JPA de base. find(classe, id) renvoie null si la ligne n'existe pas. */
@Repository
public class ReferencesExternesDaoImpl implements ReferencesExternesDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Client> trouverClient(Long id) {
        return Optional.ofNullable(em.find(Client.class, id));
    }

    @Override
    public Optional<Commercial> trouverCommercial(Long id) {
        return Optional.ofNullable(em.find(Commercial.class, id));
    }
}

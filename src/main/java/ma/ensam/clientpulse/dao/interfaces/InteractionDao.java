package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Interaction;
import ma.ensam.clientpulse.domain.enums.StatutInteraction;
import ma.ensam.clientpulse.domain.enums.TypeInteraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface InteractionDao {

    Interaction save(Interaction interaction);

    Optional<Interaction> findById(Long id);

    Page<Interaction> search(
        Long clientId,
        TypeInteraction type,
        StatutInteraction statut,
        Pageable pageable
    );
}

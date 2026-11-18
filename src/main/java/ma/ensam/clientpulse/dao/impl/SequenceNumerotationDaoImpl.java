package ma.ensam.clientpulse.dao.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.SequenceNumerotationDao;
import ma.ensam.clientpulse.dao.jpa.SequenceNumerotationJpaRepository;
import ma.ensam.clientpulse.domain.entity.SequenceNumerotation;
import ma.ensam.clientpulse.domain.enums.TypeDocument;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SequenceNumerotationDaoImpl implements SequenceNumerotationDao {

    private final SequenceNumerotationJpaRepository jpa;

    @Override
    public SequenceNumerotation verrouiller(TypeDocument type, int annee) {
        // Le compteur existe : on le verrouille. Sinon (1er document de l'année) : on le crée à 0.
        return jpa.chercherEtVerrouiller(type, annee)
                .orElseGet(() -> jpa.save(new SequenceNumerotation(type, annee, 0)));
    }

    @Override
    public SequenceNumerotation save(SequenceNumerotation sequence) {
        return jpa.save(sequence);
    }
}

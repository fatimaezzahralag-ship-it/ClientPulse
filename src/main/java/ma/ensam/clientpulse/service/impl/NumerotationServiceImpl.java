package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.SequenceNumerotationDao;
import ma.ensam.clientpulse.domain.entity.SequenceNumerotation;
import ma.ensam.clientpulse.domain.enums.TypeDocument;
import ma.ensam.clientpulse.service.interfaces.NumerotationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Numérotation continue, sans trou ni doublon.
 * Principe : on verrouille le compteur, on l'augmente de 1, on rend le numéro.
 * Le verrou est libéré à la fin de la transaction (@Transactional).
 *
 * IMPORTANT (J3) : l'émission d'une facture doit appeler cette méthode DANS LA MÊME transaction
 * que l'enregistrement de la facture. Si l'émission échoue, le compteur revient en arrière
 * avec elle : aucun numéro n'est perdu.
 */
@Service
@RequiredArgsConstructor
public class NumerotationServiceImpl implements NumerotationService {

    private final SequenceNumerotationDao sequenceDao;

    @Override
    @Transactional
    public String prochainNumero(TypeDocument type, LocalDate jour) {
        int annee = jour.getYear();
        SequenceNumerotation sequence = sequenceDao.verrouiller(type, annee);
        int numero = sequence.incrementer();
        sequenceDao.save(sequence);
        return String.format("%s-%d-%04d", prefixe(type), annee, numero);
    }

    private String prefixe(TypeDocument type) {
        return switch (type) {
            case DEVIS -> "DEV";
            case FACTURE -> "FAC";
            case AVOIR -> "AV";
        };
    }
}

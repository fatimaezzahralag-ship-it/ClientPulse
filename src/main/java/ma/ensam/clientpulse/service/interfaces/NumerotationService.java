package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.TypeDocument;

import java.time.LocalDate;

public interface NumerotationService {

    /**
     * Donne le prochain numéro du type demandé pour l'année de "jour".
     * Exemples : FAC-2026-0001, DEV-2026-0012, AV-2026-0003.
     */
    String prochainNumero(TypeDocument type, LocalDate jour);
}

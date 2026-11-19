package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.dto.StatistiqueClientDto;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Seul point d'accès aux lectures analytiques du scoring.
 *
 * Le moteur de churn, le RFM et les facteurs passent par ce DAO : ils ne
 * dépendent jamais de {@code FactureDao} ni de {@code PaiementDao}.
 */
public interface StatistiqueClientDao {

    Optional<StatistiqueClientDto> findStatistiquesByClientId(
            Long clientId,
            LocalDate jour);
}

package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.domain.entity.Client;

import java.time.LocalDate;

/**
 * Un facteur de risque est une stratégie indépendante : il reçoit un client et
 * une date de référence, et renvoie sa note (0 à 100) avec une explication.
 *
 * Le service de scoring agrège ensuite les facteurs avec leurs poids.
 */
public interface FacteurRisque {

    String code();

    ResultatFacteur evaluer(Client client, LocalDate jour);
}

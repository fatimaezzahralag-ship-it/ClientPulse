package ma.ensam.clientpulse.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import ma.ensam.clientpulse.dao.interfaces.StatistiqueClientDao;
import ma.ensam.clientpulse.dto.StatistiqueClientDto;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Agrège en une seule requête les indicateurs utilisés par les facteurs.
 *
 * Chaque indicateur est calculé dans une sous-requête indépendante : joindre
 * simultanément interactions et factures multiplierait les lignes et fausserait
 * les sommes (erreur classique du produit cartésien).
 *
 * Règle du retard moyen : pour chaque facture non annulée échéancée, on mesure
 * le nombre de jours entre l'échéance et le dernier paiement (ou la date du
 * calcul si la facture n'est pas soldée), sans jamais descendre sous zéro.
 */
@Repository
public class StatistiqueClientDaoImpl implements StatistiqueClientDao {

    private static final String REQUETE = """
            SELECT
                c.id AS clientId,
                (SELECT MAX(i.`date`)
                   FROM interactions i
                  WHERE i.client_id = c.id) AS derniereActivite,
                (SELECT COALESCE(SUM(f.total_ttc), 0)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE') AS montantFacture,
                (SELECT COUNT(*)
                   FROM interactions i
                  WHERE i.client_id = c.id
                    AND i.type = 'RECLAMATION'
                    AND i.statut <> 'CLOTUREE') AS reclamationsOuvertes,
                (SELECT COALESCE(AVG(GREATEST(0, DATEDIFF(
                            COALESCE(
                                (SELECT MAX(p.date_paiement)
                                   FROM paiement p
                                  WHERE p.facture_id = f.id),
                                :jour),
                            f.date_echeance))), 0)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE'
                    AND f.date_echeance IS NOT NULL) AS retardMoyenPaiement,
                (SELECT COALESCE(SUM(f.total_ttc), 0)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE'
                    AND f.date_emission IS NOT NULL
                    AND f.date_emission >= DATE_SUB(:jour, INTERVAL 3 MONTH)
                    AND f.date_emission <= :jour) AS montantAchats3DerniersMois,
                (SELECT COALESCE(SUM(f.total_ttc), 0)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE'
                    AND f.date_emission IS NOT NULL
                    AND f.date_emission >= DATE_SUB(:jour, INTERVAL 6 MONTH)
                    AND f.date_emission < DATE_SUB(:jour, INTERVAL 3 MONTH))
                    AS montantAchats3MoisPrecedents,
                -- Indicateurs RFM : 100 % des lectures passent par ce DAO.
                (SELECT MAX(f.date_emission)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE'
                    AND f.date_emission IS NOT NULL) AS dernierAchat,
                (SELECT MIN(f.date_emission)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE'
                    AND f.date_emission IS NOT NULL) AS premierAchat,
                (SELECT COUNT(*)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE'
                    AND f.date_emission IS NOT NULL
                    AND f.date_emission >= DATE_SUB(:jour, INTERVAL 12 MONTH)
                    AND f.date_emission <= :jour) AS nombreAchats12Mois,
                (SELECT COALESCE(SUM(f.total_ttc), 0)
                   FROM facture f
                  WHERE f.client_id = c.id
                    AND f.type = 'FACTURE'
                    AND f.statut <> 'ANNULEE'
                    AND f.date_emission IS NOT NULL
                    AND f.date_emission >= DATE_SUB(:jour, INTERVAL 12 MONTH)
                    AND f.date_emission <= :jour) AS montantAchats12Mois
            FROM clients c
            WHERE c.id = :clientId
            """;

    private final EntityManager entityManager;

    public StatistiqueClientDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<StatistiqueClientDto> findStatistiquesByClientId(
            Long clientId,
            LocalDate jour) {

        try {
            Object[] ligne = (Object[]) entityManager
                    .createNativeQuery(REQUETE)
                    .setParameter("clientId", clientId)
                    .setParameter("jour", jour)
                    .getSingleResult();

            return Optional.of(new StatistiqueClientDto(
                    clientId,
                    versDate(ligne[1]),
                    versDecimal(ligne[2]),
                    versEntier(ligne[3]),
                    versDecimal(ligne[4]),
                    versDecimal(ligne[5]),
                    versDecimal(ligne[6]),
                    versDate(ligne[7]),
                    versDate(ligne[8]),
                    versEntier(ligne[9]),
                    versDecimal(ligne[10])
            ));
        } catch (NoResultException absent) {
            return Optional.empty();
        }
    }

    private static LocalDate versDate(Object valeur) {
        if (valeur == null) {
            return null;
        }
        if (valeur instanceof LocalDate date) {
            return date;
        }
        if (valeur instanceof LocalDateTime dateHeure) {
            return dateHeure.toLocalDate();
        }
        if (valeur instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (valeur instanceof java.sql.Timestamp horodatage) {
            return horodatage.toLocalDateTime().toLocalDate();
        }
        if (valeur instanceof java.util.Date date) {
            return new java.sql.Date(date.getTime()).toLocalDate();
        }
        throw new IllegalStateException(
                "Type de date inattendu : " + valeur.getClass());
    }

    private static BigDecimal versDecimal(Object valeur) {
        if (valeur == null) {
            return BigDecimal.ZERO;
        }
        if (valeur instanceof BigDecimal decimal) {
            return decimal;
        }
        if (valeur instanceof Number nombre) {
            return new BigDecimal(nombre.toString());
        }
        return new BigDecimal(valeur.toString());
    }

    private static Long versEntier(Object valeur) {
        if (valeur == null) {
            return 0L;
        }
        if (valeur instanceof Number nombre) {
            return nombre.longValue();
        }
        return Long.valueOf(valeur.toString());
    }
}

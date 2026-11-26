package ma.ensam.clientpulse.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import ma.ensam.clientpulse.dao.interfaces.IndicateurDao;
import ma.ensam.clientpulse.dto.ClientCaDto;
import ma.ensam.clientpulse.dto.PointMensuelDto;
import ma.ensam.clientpulse.dto.SuccesRelancesDto;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Requêtes natives d'agrégation du tableau de bord.
 *
 * Chaque indicateur a sa propre requête : joindre factures, lignes et paiements dans une même requête
 * multiplierait les lignes et fausserait les sommes. Le filtre commun « facture réelle émise » est
 * {@code type = 'FACTURE' AND statut <> 'ANNULEE' AND date_emission IS NOT NULL}
 * (les brouillons n'ont pas de date d'émission).
 */
@Repository
public class IndicateurDaoImpl implements IndicateurDao {

    private static final String FACTURE_EMISE =
            "f.type = 'FACTURE' AND f.statut <> 'ANNULEE' AND f.date_emission IS NOT NULL";

    private final EntityManager entityManager;

    public IndicateurDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public long compterClientsActifs(LocalDate date) {
        return entier(requete("""
                SELECT COUNT(DISTINCT f.client_id)
                  FROM facture f
                 WHERE {{FE}}
                   AND f.date_emission > DATE_SUB(:date, INTERVAL 12 MONTH)
                   AND f.date_emission <= :date
                """.replace("{{FE}}", FACTURE_EMISE))
                .setParameter("date", date).getSingleResult());
    }

    @Override
    public long compterNouveauxClients(LocalDate debutExclu, LocalDate finInclus) {
        return entier(avecPeriode(requete("""
                SELECT COUNT(*)
                  FROM (SELECT f.client_id, MIN(f.date_emission) AS premiere
                          FROM facture f
                         WHERE {{FE}}
                         GROUP BY f.client_id) t
                 WHERE t.premiere > :debut AND t.premiere <= :fin
                """.replace("{{FE}}", FACTURE_EMISE)), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public long compterFacturesEmises(LocalDate debutExclu, LocalDate finInclus) {
        return entier(avecPeriode(requete("""
                SELECT COUNT(*)
                  FROM facture f
                 WHERE {{FE}} AND f.date_emission > :debut AND f.date_emission <= :fin
                """.replace("{{FE}}", FACTURE_EMISE)), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public BigDecimal margeMoyenneParFacture(LocalDate debutExclu, LocalDate finInclus) {
        // Marge d'une facture = somme sur ses lignes de (montant HT de la ligne x marge du produit / 100).
        // On ne garde que les factures ayant au moins une ligne avec une marge connue.
        return decimal(avecPeriode(requete("""
                SELECT AVG(t.marge_facture)
                  FROM (SELECT f.id,
                               SUM(COALESCE(lf.montant_ht,
                                            lf.quantite * lf.prix_unitaire_ht * (1 - COALESCE(lf.remise, 0) / 100))
                                   * p.marge / 100) AS marge_facture
                          FROM facture f
                          JOIN ligne_facture lf ON lf.facture_id = f.id
                          JOIN produit p ON p.id = lf.produit_id
                         WHERE {{FE}} AND p.marge IS NOT NULL
                           AND f.date_emission > :debut AND f.date_emission <= :fin
                         GROUP BY f.id) t
                """.replace("{{FE}}", FACTURE_EMISE)), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public BigDecimal chiffreAffairesHt(LocalDate debutExclu, LocalDate finInclus) {
        return decimal(avecPeriode(requete("""
                SELECT COALESCE(SUM(f.total_ht), 0)
                  FROM facture f
                 WHERE {{FE}} AND f.date_emission > :debut AND f.date_emission <= :fin
                """.replace("{{FE}}", FACTURE_EMISE)), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public BigDecimal totalFactureTtc(LocalDate debutExclu, LocalDate finInclus) {
        return decimal(avecPeriode(requete("""
                SELECT COALESCE(SUM(f.total_ttc), 0)
                  FROM facture f
                 WHERE {{FE}} AND f.date_emission > :debut AND f.date_emission <= :fin
                """.replace("{{FE}}", FACTURE_EMISE)), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public BigDecimal totalEncaisse(LocalDate debutExclu, LocalDate finInclus) {
        return decimal(avecPeriode(requete("""
                SELECT COALESCE(SUM(p.montant), 0)
                  FROM paiement p
                  JOIN facture f ON f.id = p.facture_id
                 WHERE f.type = 'FACTURE' AND f.statut <> 'ANNULEE'
                   AND p.date_paiement > :debut AND p.date_paiement <= :fin
                """), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public BigDecimal delaiMoyenPaiement(LocalDate debutExclu, LocalDate finInclus) {
        return decimal(avecPeriode(requete("""
                SELECT AVG(DATEDIFF(t.dernier_paiement, t.date_emission))
                  FROM (SELECT f.id, f.date_emission, MAX(p.date_paiement) AS dernier_paiement
                          FROM facture f
                          JOIN paiement p ON p.facture_id = f.id
                         WHERE f.type = 'FACTURE' AND f.statut = 'PAYEE' AND f.date_emission IS NOT NULL
                         GROUP BY f.id, f.date_emission) t
                 WHERE t.dernier_paiement > :debut AND t.dernier_paiement <= :fin
                """), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public BigDecimal montantFactureEcheance(LocalDate debutExclu, LocalDate finInclus) {
        return decimal(avecPeriode(requete("""
                SELECT COALESCE(SUM(f.total_ttc), 0)
                  FROM facture f
                 WHERE f.type = 'FACTURE'
                   AND f.statut IN ('EMISE', 'PARTIELLEMENT_PAYEE', 'EN_RETARD', 'PAYEE')
                   AND f.date_echeance > :debut AND f.date_echeance <= :fin
                """), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public BigDecimal encaisseSurFacturesEcheance(LocalDate debutExclu, LocalDate finInclus) {
        return decimal(avecPeriode(requete("""
                SELECT COALESCE(SUM(p.montant), 0)
                  FROM paiement p
                  JOIN facture f ON f.id = p.facture_id
                 WHERE f.type = 'FACTURE'
                   AND f.statut IN ('EMISE', 'PARTIELLEMENT_PAYEE', 'EN_RETARD', 'PAYEE')
                   AND f.date_echeance > :debut AND f.date_echeance <= :fin
                   AND p.date_paiement <= :fin
                """), debutExclu, finInclus).getSingleResult());
    }

    @Override
    public SuccesRelancesDto succesRelances(LocalDate debutExclu, LocalDate finInclus) {
        Object[] ligne = (Object[]) avecPeriode(requete("""
                SELECT COUNT(*),
                       COALESCE(SUM(CASE
                           WHEN r.nature = 'PAIEMENT' AND EXISTS (
                                SELECT 1 FROM paiement p
                                 WHERE p.facture_id = r.facture_id
                                   AND p.date_paiement >= DATE(r.date_creation)
                                   AND p.date_paiement <= DATE_ADD(DATE(r.date_creation), INTERVAL 30 DAY))
                           THEN 1
                           WHEN r.nature = 'COMMERCIALE' AND EXISTS (
                                SELECT 1 FROM facture f
                                 WHERE f.client_id = r.client_id
                                   AND f.type = 'FACTURE' AND f.statut <> 'ANNULEE'
                                   AND f.date_emission >= DATE(r.date_creation)
                                   AND f.date_emission <= DATE_ADD(DATE(r.date_creation), INTERVAL 30 DAY))
                           THEN 1
                           ELSE 0 END), 0)
                  FROM relances r
                 WHERE r.statut = 'TERMINEE'
                   AND DATE(r.date_creation) > :debut AND DATE(r.date_creation) <= :fin
                """), debutExclu, finInclus).getSingleResult();
        return new SuccesRelancesDto(entier(ligne[0]), entier(ligne[1]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PointMensuelDto> facturationMensuelle(LocalDate debutExclu, LocalDate finInclus) {
        List<Object[]> lignes = avecPeriode(requete("""
                SELECT DATE_FORMAT(f.date_emission, '%Y-%m') AS mois, SUM(f.total_ttc)
                  FROM facture f
                 WHERE {{FE}} AND f.date_emission > :debut AND f.date_emission <= :fin
                 GROUP BY DATE_FORMAT(f.date_emission, '%Y-%m')
                 ORDER BY DATE_FORMAT(f.date_emission, '%Y-%m')
                """.replace("{{FE}}", FACTURE_EMISE)), debutExclu, finInclus).getResultList();
        return lignes.stream()
                .map(l -> new PointMensuelDto(String.valueOf(l[0]), decimal(l[1]), BigDecimal.ZERO))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PointMensuelDto> encaissementMensuel(LocalDate debutExclu, LocalDate finInclus) {
        List<Object[]> lignes = avecPeriode(requete("""
                SELECT DATE_FORMAT(p.date_paiement, '%Y-%m') AS mois, SUM(p.montant)
                  FROM paiement p
                  JOIN facture f ON f.id = p.facture_id
                 WHERE f.type = 'FACTURE' AND f.statut <> 'ANNULEE'
                   AND p.date_paiement > :debut AND p.date_paiement <= :fin
                 GROUP BY DATE_FORMAT(p.date_paiement, '%Y-%m')
                 ORDER BY DATE_FORMAT(p.date_paiement, '%Y-%m')
                """), debutExclu, finInclus).getResultList();
        return lignes.stream()
                .map(l -> new PointMensuelDto(String.valueOf(l[0]), BigDecimal.ZERO, decimal(l[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ClientCaDto> meilleursClients(LocalDate debutExclu, LocalDate finInclus, int limite) {
        List<Object[]> lignes = avecPeriode(requete("""
                SELECT c.id, c.raison_sociale, SUM(f.total_ht) AS ca
                  FROM facture f
                  JOIN clients c ON c.id = f.client_id
                 WHERE {{FE}} AND f.date_emission > :debut AND f.date_emission <= :fin
                 GROUP BY c.id, c.raison_sociale
                 ORDER BY ca DESC, c.raison_sociale
                """.replace("{{FE}}", FACTURE_EMISE)), debutExclu, finInclus)
                .setMaxResults(limite)
                .getResultList();
        return lignes.stream()
                .map(l -> new ClientCaDto(((Number) l[0]).longValue(), String.valueOf(l[1]), decimal(l[2])))
                .toList();
    }

    // ------------------------------------------------------------------ utilitaires

    private Query requete(String sql) {
        return entityManager.createNativeQuery(sql);
    }

    private static Query avecPeriode(Query requete, LocalDate debutExclu, LocalDate finInclus) {
        return requete.setParameter("debut", debutExclu).setParameter("fin", finInclus);
    }

    private static long entier(Object valeur) {
        return valeur == null ? 0L : ((Number) valeur).longValue();
    }

    private static BigDecimal decimal(Object valeur) {
        if (valeur == null) {
            return BigDecimal.ZERO;
        }
        if (valeur instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(valeur.toString());
    }
}

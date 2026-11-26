package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.dao.interfaces.RelanceDao;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.ResultatTraitement;
import ma.ensam.clientpulse.service.interfaces.FacturationService;
import ma.ensam.clientpulse.service.interfaces.RelancePaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class RelancePaiementServiceImpl implements RelancePaiementService {

    private final FactureDao factureDao;
    private final RelanceDao relanceDao;
    private final FacturationService facturationService;
    private final DelaisRelance delais;

    /** Constructeur historique : délais par défaut (J-3, J+3, J+15, J+30, J+45). Utilisé par les tests. */
    public RelancePaiementServiceImpl(FactureDao factureDao,
                                      RelanceDao relanceDao,
                                      FacturationService facturationService) {
        this(factureDao, relanceDao, facturationService, DelaisRelance.parDefaut());
    }

    /** Constructeur utilisé par Spring : les délais viennent de la table des paramètres. */
    @Autowired
    public RelancePaiementServiceImpl(FactureDao factureDao,
                                      RelanceDao relanceDao,
                                      FacturationService facturationService,
                                      DelaisRelance delais) {
        this.factureDao = factureDao;
        this.relanceDao = relanceDao;
        this.facturationService = facturationService;
        this.delais = delais;
    }

    @Override
    @Transactional
    public int genererRelances(LocalDate jour) {
        int creees = 0;
        int[] decalages = delais.decalages();   // lus une seule fois par passage
        for (Facture facture : factureDao.findOuvertes()) {
            if (facture.getDateEcheance() == null || facture.getResteAPayer().signum() <= 0) {
                continue;   // rien à relancer
            }
            long joursApresEcheance = ChronoUnit.DAYS.between(facture.getDateEcheance(), jour);
            int niveau = NiveauxRelance.niveauAtteint(joursApresEcheance, decalages);
            if (niveau == 0) {
                continue;   // l'échéance est encore trop loin
            }
            // Une seule relance par niveau et par facture, même si le job tourne plusieurs fois
            if (relanceDao.existePourFactureEtNiveau(facture.getId(), NatureRelance.PAIEMENT, niveau)) {
                continue;
            }
            relanceDao.save(construire(facture, niveau, jour, decalages));
            creees++;
        }
        return creees;
    }

    // @Transactional ici : traiterJour appelle genererRelances sur "this", ce qui ne passe pas par le proxy
    // Spring. Une seule transaction pour les deux étapes : si l'une échoue, rien n'est enregistré.
    @Override
    @Transactional
    public ResultatTraitement traiterJour(LocalDate jour) {
        // D'abord les statuts (EN_RETARD), ensuite les relances
        int enRetard = facturationService.marquerEnRetard(jour);
        int relances = genererRelances(jour);
        return new ResultatTraitement(enRetard, relances);
    }

    private Relance construire(Facture facture, int niveau, LocalDate jour, int[] decalages) {
        Relance relance = new Relance();
        relance.setNature(NatureRelance.PAIEMENT);
        relance.setStatut(StatutRelance.A_FAIRE);
        relance.setClient(facture.getClient());
        // Le responsable du client reçoit la relance (peut rester vide)
        relance.setCommercial(facture.getClient().getCommercial());
        relance.setFacture(facture);
        relance.setNiveau(niveau);
        relance.setDateEcheance(jour);
        relance.setMotif("Relance " + NiveauxRelance.libelle(niveau, decalages) + " : facture " + facture.getNumero()
                + ", reste à payer " + facture.getResteAPayer().setScale(2, RoundingMode.HALF_UP).toPlainString()
                + " MAD, échéance le " + facture.getDateEcheance() + ".");
        return relance;
    }
}

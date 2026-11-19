package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.dao.interfaces.RelanceDao;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.ResultatTraitement;
import ma.ensam.clientpulse.service.interfaces.FacturationService;
import ma.ensam.clientpulse.service.interfaces.RelancePaiementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RelancePaiementServiceImpl implements RelancePaiementService {

    private final FactureDao factureDao;
    private final RelanceDao relanceDao;
    private final FacturationService facturationService;

    @Override
    @Transactional
    public int genererRelances(LocalDate jour) {
        int creees = 0;
        for (Facture facture : factureDao.findOuvertes()) {
            if (facture.getDateEcheance() == null || facture.getResteAPayer().signum() <= 0) {
                continue;   // rien à relancer
            }
            long joursApresEcheance = ChronoUnit.DAYS.between(facture.getDateEcheance(), jour);
            int niveau = NiveauxRelance.niveauAtteint(joursApresEcheance);
            if (niveau == 0) {
                continue;   // l'échéance est encore trop loin
            }
            if (relanceDao.existePourFactureEtNiveau(facture.getId(), NatureRelance.PAIEMENT, niveau)) {
                continue;
            }
            relanceDao.save(construire(facture, niveau, jour));
            creees++;
        }
        return creees;
    }


    @Override
    @Transactional
    public ResultatTraitement traiterJour(LocalDate jour) {
        int enRetard = facturationService.marquerEnRetard(jour);
        int relances = genererRelances(jour);
        return new ResultatTraitement(enRetard, relances);
    }

    private Relance construire(Facture facture, int niveau, LocalDate jour) {
        Relance relance = new Relance();
        relance.setNature(NatureRelance.PAIEMENT);
        relance.setNiveau(niveau);
        relance.setDateCreation(LocalDateTime.now());
        relance.setEcheance(jour);
        relance.setStatut(StatutRelance.A_FAIRE);
        relance.setSuggereeParSysteme(true);
        relance.setClient(facture.getClient());
        relance.setFacture(facture);
        relance.setContenu("Relance " + NiveauxRelance.libelle(niveau) + " : facture " + facture.getNumero()
                + ", reste à payer " + facture.getResteAPayer().setScale(2, RoundingMode.HALF_UP).toPlainString()
                + " MAD, échéance le " + facture.getDateEcheance() + ".");
        return relance;
    }
}

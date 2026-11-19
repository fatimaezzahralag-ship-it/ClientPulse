package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.DevisDao;
import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.domain.entity.Devis;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.LigneDevis;
import ma.ensam.clientpulse.domain.entity.LigneFacture;
import ma.ensam.clientpulse.domain.enums.StatutDevis;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeDocument;
import ma.ensam.clientpulse.domain.enums.TypeFacture;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.Totaux;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.CalculateurMontants;
import ma.ensam.clientpulse.service.interfaces.FacturationService;
import ma.ensam.clientpulse.service.interfaces.NumerotationService;
import ma.ensam.clientpulse.service.mapper.FactureMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturationServiceImpl implements FacturationService {

    static final int DELAI_PAIEMENT_PAR_DEFAUT_JOURS = 30;

    private final FactureDao factureDao;
    private final DevisDao devisDao;
    private final CalculateurMontants calculateur;
    private final NumerotationService numerotationService;
    private final FactureMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<FactureDto> lister(StatutFacture statut, Pageable pageable) {
        Page<Facture> page = statut == null
                ? factureDao.findAll(pageable)
                : factureDao.findByStatut(statut, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public FactureDto trouver(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    public FactureDto creerDepuisDevis(Long devisId) {
        Devis devis = devisDao.findById(devisId)
                .orElseThrow(() -> RessourceIntrouvableException.de("Devis", devisId));
        if (devis.getStatut() != StatutDevis.ACCEPTE) {
            throw new RegleMetierException("Seul un devis accepté peut être facturé (statut actuel : "
                    + devis.getStatut() + ").");
        }
        if (factureDao.existePourDevis(devisId)) {
            throw new RegleMetierException("Ce devis a déjà été facturé.");
        }

        Facture facture = new Facture();
        facture.setType(TypeFacture.FACTURE);
        facture.setClient(devis.getClient());
        facture.setDevis(devis);
        for (LigneDevis ld : devis.getLignes()) {
            LigneFacture lf = new LigneFacture();
            lf.setProduit(ld.getProduit());
            lf.setDesignation(ld.getDesignation());
            lf.setQuantite(ld.getQuantite());
            lf.setPrixUnitaireHT(ld.getPrixUnitaireHT());
            lf.setRemise(ld.getRemise());
            lf.setTauxTVA(ld.getTauxTVA());   // le taux est COPIÉ : un changement futur ne touche pas la facture
            facture.ajouterLigne(lf);
        }
        recalculer(facture);
        return mapper.toDto(factureDao.save(facture));
    }

    @Override
    public FactureDto emettre(Long factureId, LocalDate dateEmission, LocalDate dateEcheance) {
        Facture facture = factureDao.findByIdPourMiseAJour(factureId)
                .orElseThrow(() -> RessourceIntrouvableException.de("Facture", factureId));

        LocalDate emission = dateEmission != null ? dateEmission : LocalDate.now();
        LocalDate echeance = dateEcheance != null ? dateEcheance : emission.plusDays(DELAI_PAIEMENT_PAR_DEFAUT_JOURS);
        if (echeance.isBefore(emission)) {
            throw new RegleMetierException("L'échéance est antérieure à la date d'émission.");
        }

        String numero = numerotationService.prochainNumero(TypeDocument.FACTURE, emission);
        facture.setDateEmission(emission);
        facture.setDateEcheance(echeance);
        facture.emettre(numero);
        return mapper.toDto(factureDao.save(facture));
    }

    @Override
    public FactureDto annuler(Long factureId) {
        Facture facture = factureDao.findByIdPourMiseAJour(factureId)
                .orElseThrow(() -> RessourceIntrouvableException.de("Facture", factureId));
        facture.annuler();
        return mapper.toDto(factureDao.save(facture));
    }

    @Override
    public int marquerEnRetard(LocalDate jour) {
        int compteur = 0;
        for (Facture facture : factureDao.findOuvertes()) {
            if (facture.marquerEnRetard(jour)) {
                compteur++;
            }
        }
        return compteur;
    }


    private Facture charger(Long id) {
        return factureDao.findById(id).orElseThrow(() -> RessourceIntrouvableException.de("Facture", id));
    }

    private void recalculer(Facture facture) {
        for (LigneFacture ligne : facture.getLignes()) {
            ligne.setMontantHT(calculateur.montantHT(ligne));
            ligne.setMontantTVA(calculateur.montantTVA(ligne));
        }
        Totaux totaux = calculateur.calculer(facture.getLignes());
        facture.setTotalHT(totaux.totalHT());
        facture.setTotalTVA(totaux.totalTVA());
        facture.setTotalTTC(totaux.totalTTC());
    }
}

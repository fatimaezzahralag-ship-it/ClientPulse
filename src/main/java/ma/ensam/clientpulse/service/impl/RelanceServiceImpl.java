package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.dao.interfaces.ReferencesExternesDao;
import ma.ensam.clientpulse.dao.interfaces.RelanceDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.RelanceDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.RelanceService;
import ma.ensam.clientpulse.service.mapper.RelanceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RelanceServiceImpl implements RelanceService {

    private final RelanceDao relanceDao;
    private final FactureDao factureDao;
    private final ReferencesExternesDao references;
    private final RelanceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<RelanceDto> lister(NatureRelance nature, Pageable pageable) {
        Page<Relance> page = nature == null
                ? relanceDao.findAll(pageable)
                : relanceDao.findByNature(nature, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public RelanceDto trouver(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    public RelanceDto creer(RelanceDto dto) {
        Client client = references.trouverClient(dto.clientId())
                .orElseThrow(() -> RessourceIntrouvableException.de("Client", dto.clientId()));

        Relance relance = new Relance();
        relance.setNature(dto.nature());
        relance.setNiveau(dto.niveau());
        relance.setDateCreation(LocalDateTime.now());
        relance.setEcheance(dto.echeance());
        relance.setStatut(StatutRelance.A_FAIRE);
        relance.setContenu(dto.contenu());
        relance.setSuggereeParSysteme(dto.suggereeParSysteme());
        relance.setAffecteAId(dto.affecteAId());
        relance.setClient(client);

        if (dto.factureId() != null) {
            Facture facture = factureDao.findById(dto.factureId())
                    .orElseThrow(() -> RessourceIntrouvableException.de("Facture", dto.factureId()));
            relance.setFacture(facture);
        }
        if (dto.nature() == NatureRelance.PAIEMENT) {
            if (dto.factureId() == null) {
                throw new RegleMetierException("Une relance de paiement doit viser une facture.");
            }
            if (relanceDao.existePourFactureEtNiveau(dto.factureId(), NatureRelance.PAIEMENT, dto.niveau())) {
                throw new RegleMetierException("Cette facture a déjà une relance de niveau " + dto.niveau() + ".");
            }
        }
        return mapper.toDto(relanceDao.save(relance));
    }

    @Override
    public RelanceDto changerStatut(Long id, StatutRelance statut, String resultat) {
        Relance relance = charger(id);
        relance.setStatut(statut);
        if (resultat != null) {
            relance.setResultat(resultat);
        }
        return mapper.toDto(relanceDao.save(relance));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePourFacture(Long factureId, NatureRelance nature, int niveau) {
        return relanceDao.existePourFactureEtNiveau(factureId, nature, niveau);
    }

    private Relance charger(Long id) {
        return relanceDao.findById(id).orElseThrow(() -> RessourceIntrouvableException.de("Relance", id));
    }
}

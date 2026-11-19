package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.CommercialDao;
import ma.ensam.clientpulse.dao.interfaces.RelanceDao;
import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.ResultatRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.RelanceDto;
import ma.ensam.clientpulse.dto.RelanceRequest;
import ma.ensam.clientpulse.exception.DoublonException;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.RelanceService;
import ma.ensam.clientpulse.service.mapper.RelanceMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Implémentation du contrat commun des relances. */
@Service
@Transactional
public class RelanceServiceImpl implements RelanceService {

    private final RelanceDao relanceDao;
    private final ClientDao clientDao;
    private final CommercialDao commercialDao;
    private final RelanceMapper relanceMapper;

    public RelanceServiceImpl(
            RelanceDao relanceDao,
            ClientDao clientDao,
            CommercialDao commercialDao,
            RelanceMapper relanceMapper) {
        this.relanceDao = relanceDao;
        this.clientDao = clientDao;
        this.commercialDao = commercialDao;
        this.relanceMapper = relanceMapper;
    }

    @Override
    public RelanceDto creer(RelanceRequest request) {

        var client = clientDao.findById(request.clientId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Client introuvable : " + request.clientId()));

        if (request.nature() == null) {
            throw new RegleMetierException("La nature de la relance est obligatoire");
        }
        if (request.motif() == null || request.motif().isBlank()) {
            throw new RegleMetierException("Le motif de la relance est obligatoire");
        }

        boolean dejaActive = relanceDao
                .findActiveByClientAndNature(
                        request.clientId(), request.nature())
                .isPresent();
        if (dejaActive) {
            throw new DoublonException(
                    "Une relance " + request.nature()
                            + " est déjà active pour ce client");
        }

        Long commercialId = request.commercialId() != null
                ? request.commercialId()
                : (client.getCommercial() != null
                        ? client.getCommercial().getId()
                        : null);

        var commercial = commercialId != null
                ? commercialDao.findById(commercialId).orElse(null)
                : null;

        Relance relance = new Relance();
        relance.setClient(client);
        relance.setCommercial(commercial);
        relance.setNature(request.nature());
        relance.setStatut(StatutRelance.A_FAIRE);
        relance.setMotif(request.motif().trim());
        relance.setDateEcheance(request.dateEcheance());

        return relanceMapper.toDto(relanceDao.save(relance));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelanceDto> relanceActive(
            Long clientId, NatureRelance nature) {
        return relanceDao.findActiveByClientAndNature(clientId, nature)
                .map(relanceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelanceDto> findById(Long id) {
        return relanceDao.findById(id).map(relanceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RelanceDto> rechercher(
            Long commercialId, StatutRelance statut, Pageable pageable) {
        return relanceDao.rechercher(commercialId, statut, pageable)
                .map(relanceMapper::toDto);
    }

    @Override
    public RelanceDto demarrer(Long relanceId) {
        Relance relance = trouver(relanceId);

        if (relance.getStatut() == StatutRelance.TERMINEE) {
            throw new RegleMetierException(
                    "Impossible de démarrer une relance terminée");
        }

        relance.setStatut(StatutRelance.EN_COURS);
        return relanceMapper.toDto(relanceDao.save(relance));
    }

    @Override
    public RelanceDto traiter(
            Long relanceId,
            ResultatRelance resultat,
            String commentaire) {

        if (resultat == null) {
            throw new RegleMetierException("Le résultat de la relance est obligatoire");
        }

        Relance relance = trouver(relanceId);
        relance.setStatut(StatutRelance.TERMINEE);
        relance.setResultat(resultat);
        relance.setCommentaire(commentaire);
        return relanceMapper.toDto(relanceDao.save(relance));
    }

    private Relance trouver(Long relanceId) {
        return relanceDao.findById(relanceId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Relance introuvable : " + relanceId));
    }
}

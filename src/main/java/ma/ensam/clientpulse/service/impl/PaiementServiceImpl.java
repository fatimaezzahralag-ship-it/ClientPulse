package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.FactureDao;
import ma.ensam.clientpulse.dao.interfaces.PaiementDao;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.PaiementDto;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.PaiementService;
import ma.ensam.clientpulse.service.mapper.FactureMapper;
import ma.ensam.clientpulse.service.mapper.PaiementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementServiceImpl implements PaiementService {

    private final FactureDao factureDao;
    private final PaiementDao paiementDao;
    private final PaiementMapper paiementMapper;
    private final FactureMapper factureMapper;

    @Override
    public FactureDto enregistrer(Long factureId, PaiementDto dto) {
        Facture facture = factureDao.findByIdPourMiseAJour(factureId)
                .orElseThrow(() -> RessourceIntrouvableException.de("Facture", factureId));

        facture.enregistrerPaiement(paiementMapper.versEntite(dto));

        return factureMapper.toDto(factureDao.save(facture));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementDto> lister(Long factureId) {
        if (factureDao.findById(factureId).isEmpty()) {
            throw RessourceIntrouvableException.de("Facture", factureId);
        }
        return paiementDao.findByFactureId(factureId).stream().map(paiementMapper::toDto).toList();
    }
}

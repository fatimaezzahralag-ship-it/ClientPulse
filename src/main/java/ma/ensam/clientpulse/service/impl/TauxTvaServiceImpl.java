package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.TauxTvaDao;
import ma.ensam.clientpulse.domain.entity.TauxTVA;
import ma.ensam.clientpulse.dto.TauxTvaDto;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.TauxTvaService;
import ma.ensam.clientpulse.service.mapper.TauxTvaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TauxTvaServiceImpl implements TauxTvaService {

    private final TauxTvaDao tauxTvaDao;
    private final TauxTvaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TauxTvaDto> lister(boolean actifsSeulement) {
        List<TauxTVA> taux = actifsSeulement ? tauxTvaDao.findByActif(true) : tauxTvaDao.findAll();
        return taux.stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TauxTvaDto trouver(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    public TauxTvaDto creer(TauxTvaDto dto) {
        TauxTVA taux = new TauxTVA();
        mapper.copier(dto, taux);
        taux.setActif(true);
        return mapper.toDto(tauxTvaDao.save(taux));
    }

    @Override
    public TauxTvaDto modifier(Long id, TauxTvaDto dto) {
        TauxTVA taux = charger(id);
        mapper.copier(dto, taux);
        return mapper.toDto(tauxTvaDao.save(taux));
    }

    @Override
    public TauxTvaDto desactiver(Long id) {
        TauxTVA taux = charger(id);
        taux.setActif(false);
        return mapper.toDto(tauxTvaDao.save(taux));
    }

    private TauxTVA charger(Long id) {
        return tauxTvaDao.findById(id).orElseThrow(() -> RessourceIntrouvableException.de("Taux de TVA", id));
    }
}

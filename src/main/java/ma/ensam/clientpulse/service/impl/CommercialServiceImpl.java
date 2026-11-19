package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.CommercialDao;
import ma.ensam.clientpulse.dto.CommercialDto;
import ma.ensam.clientpulse.service.interfaces.CommercialService;
import ma.ensam.clientpulse.service.mapper.CommercialMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommercialServiceImpl implements CommercialService {

    private final CommercialDao commercialDao;
    private final CommercialMapper commercialMapper;

    public CommercialServiceImpl(
            CommercialDao commercialDao,
            CommercialMapper commercialMapper) {
        this.commercialDao = commercialDao;
        this.commercialMapper = commercialMapper;
    }

    @Override
    public List<CommercialDto> findAll() {
        return commercialDao.findAll().stream()
                .map(commercialMapper::toDto)
                .sorted(Comparator
                        .comparing(
                                (CommercialDto dto) -> valeur(dto.nom()),
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(
                                dto -> valeur(dto.prenom()),
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static String valeur(String texte) {
        return texte != null ? texte : "";
    }
}

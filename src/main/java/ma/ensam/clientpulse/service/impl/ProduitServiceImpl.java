package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.ProduitDao;
import ma.ensam.clientpulse.dao.interfaces.TauxTvaDao;
import ma.ensam.clientpulse.domain.entity.Produit;
import ma.ensam.clientpulse.domain.entity.TauxTVA;
import ma.ensam.clientpulse.dto.ProduitDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.ProduitService;
import ma.ensam.clientpulse.service.mapper.ProduitMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduitServiceImpl implements ProduitService {

    private final ProduitDao produitDao;
    private final TauxTvaDao tauxTvaDao;
    private final ProduitMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProduitDto> lister(String motCle, Pageable pageable) {
        Page<Produit> page = (motCle == null || motCle.isBlank())
                ? produitDao.findAll(pageable)
                : produitDao.rechercher(motCle.trim(), pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProduitDto trouver(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    public ProduitDto creer(ProduitDto dto) {
        if (produitDao.findByReference(dto.reference()).isPresent()) {
            throw new RegleMetierException("La référence " + dto.reference() + " est déjà utilisée.");
        }
        Produit produit = new Produit();
        mapper.copier(dto, produit, chargerTauxActif(dto.tauxTvaId()));
        produit.setActif(true);
        return mapper.toDto(produitDao.save(produit));
    }

    @Override
    public ProduitDto modifier(Long id, ProduitDto dto) {
        Produit produit = charger(id);
        // La référence doit rester unique : on refuse si un AUTRE produit la porte déjà
        produitDao.findByReference(dto.reference()).ifPresent(autre -> {
            if (!autre.getId().equals(id)) {
                throw new RegleMetierException("La référence " + dto.reference() + " est déjà utilisée.");
            }
        });
        mapper.copier(dto, produit, chargerTauxActif(dto.tauxTvaId()));
        return mapper.toDto(produitDao.save(produit));
    }

    @Override
    public ProduitDto desactiver(Long id) {
        Produit produit = charger(id);
        produit.setActif(false);
        return mapper.toDto(produitDao.save(produit));
    }

    private Produit charger(Long id) {
        return produitDao.findById(id).orElseThrow(() -> RessourceIntrouvableException.de("Produit", id));
    }

    private TauxTVA chargerTauxActif(Long tauxId) {
        TauxTVA taux = tauxTvaDao.findById(tauxId)
                .orElseThrow(() -> RessourceIntrouvableException.de("Taux de TVA", tauxId));
        if (!taux.isActif()) {
            throw new RegleMetierException("Le taux de TVA \"" + taux.getLibelle() + "\" est désactivé.");
        }
        return taux;
    }
}

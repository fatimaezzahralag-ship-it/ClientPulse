package ma.ensam.clientpulse.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.DevisDao;
import ma.ensam.clientpulse.dao.interfaces.ProduitDao;
import ma.ensam.clientpulse.dao.interfaces.ReferencesExternesDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Commercial;
import ma.ensam.clientpulse.domain.entity.Devis;
import ma.ensam.clientpulse.domain.entity.Produit;
import ma.ensam.clientpulse.domain.enums.TypeDocument;
import ma.ensam.clientpulse.dto.DevisDto;
import ma.ensam.clientpulse.dto.LigneDto;
import ma.ensam.clientpulse.dto.Totaux;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.CalculateurMontants;
import ma.ensam.clientpulse.service.interfaces.DevisService;
import ma.ensam.clientpulse.service.interfaces.NumerotationService;
import ma.ensam.clientpulse.service.mapper.DevisMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class DevisServiceImpl implements DevisService {

    private final DevisDao devisDao;
    private final ProduitDao produitDao;
    private final ReferencesExternesDao references;
    private final CalculateurMontants calculateur;
    private final NumerotationService numerotationService;
    private final DevisMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DevisDto> lister(Pageable pageable) {
        return devisDao.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DevisDto trouver(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    public DevisDto creer(DevisDto dto) {
        Devis devis = new Devis();
        LocalDate emission = dto.dateEmission() != null ? dto.dateEmission() : LocalDate.now();
        devis.setDateEmission(emission);
        remplir(devis, dto);
        // Le numéro est pris dans la même transaction que l'enregistrement : si tout échoue, il est rendu
        devis.setNumero(numerotationService.prochainNumero(TypeDocument.DEVIS, emission));
        return mapper.toDto(devisDao.save(devis));
    }

    @Override
    public DevisDto modifier(Long id, DevisDto dto) {
        Devis devis = charger(id);
        if (!devis.estModifiable()) {
            throw new RegleMetierException("Seul un devis brouillon peut être modifié.");
        }
        if (dto.dateEmission() != null) {
            devis.setDateEmission(dto.dateEmission());
        }
        // orphanRemoval = true : les anciennes lignes retirées de la liste sont supprimées en base
        devis.getLignes().clear();
        remplir(devis, dto);
        return mapper.toDto(devisDao.save(devis));
    }

    @Override
    public void supprimer(Long id) {
        Devis devis = charger(id);
        if (!devis.estModifiable()) {
            throw new RegleMetierException("Seul un devis brouillon peut être supprimé.");
        }
        devisDao.deleteById(id);
    }

    @Override
    public DevisDto envoyer(Long id) {
        Devis devis = charger(id);
        devis.envoyer();
        return mapper.toDto(devisDao.save(devis));
    }

    @Override
    public DevisDto accepter(Long id) {
        Devis devis = charger(id);
        devis.accepter(LocalDate.now());
        return mapper.toDto(devisDao.save(devis));
    }

    @Override
    public DevisDto refuser(Long id) {
        Devis devis = charger(id);
        devis.refuser();
        return mapper.toDto(devisDao.save(devis));
    }

    // ------------------------------------------------------------------ privé

    private Devis charger(Long id) {
        return devisDao.findById(id).orElseThrow(() -> RessourceIntrouvableException.de("Devis", id));
    }

    /** Recopie client, commercial, validité et lignes du DTO dans le devis, puis recalcule les totaux. */
    private void remplir(Devis devis, DevisDto dto) {
        Client client = references.trouverClient(dto.clientId())
                .orElseThrow(() -> RessourceIntrouvableException.de("Client", dto.clientId()));
        devis.setClient(client);

        if (dto.commercialId() != null) {
            Commercial commercial = references.trouverCommercial(dto.commercialId())
                    .orElseThrow(() -> RessourceIntrouvableException.de("Commercial", dto.commercialId()));
            devis.setCommercial(commercial);
        } else {
            devis.setCommercial(null);
        }

        if (dto.dateValidite() != null && dto.dateValidite().isBefore(devis.getDateEmission())) {
            throw new RegleMetierException("La date de validité est antérieure à la date d'émission.");
        }
        devis.setDateValidite(dto.dateValidite());

        for (LigneDto ligneDto : dto.lignes()) {
            Produit produit = null;
            if (ligneDto.produitId() != null) {
                produit = produitDao.findById(ligneDto.produitId())
                        .orElseThrow(() -> RessourceIntrouvableException.de("Produit", ligneDto.produitId()));
            }
            devis.ajouterLigne(mapper.versLigne(ligneDto, produit));
        }

        // Un seul endroit calcule les montants : le CalculateurMontants
        Totaux totaux = calculateur.calculer(devis.getLignes());
        devis.setTotalHT(totaux.totalHT());
        devis.setTotalTVA(totaux.totalTVA());
        devis.setTotalTTC(totaux.totalTTC());
    }
}

package ma.ensam.clientpulse.service.mapper;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.domain.entity.Devis;
import ma.ensam.clientpulse.domain.entity.LigneDevis;
import ma.ensam.clientpulse.domain.entity.Produit;
import ma.ensam.clientpulse.dto.DevisDto;
import ma.ensam.clientpulse.dto.LigneDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.service.interfaces.CalculateurMontants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DevisMapper {

    // Le calculateur remplit montantHT / montantTVA de chaque ligne affichée
    private final CalculateurMontants calculateur;

    public DevisDto toDto(Devis d) {
        List<LigneDto> lignes = d.getLignes().stream().map(this::ligneToDto).toList();
        Long commercialId = d.getCommercial() == null ? null : d.getCommercial().getId();
        return new DevisDto(d.getId(), d.getNumero(), d.getDateEmission(), d.getDateValidite(), d.getStatut(),
                d.getClient().getId(), commercialId, lignes, d.getTotalHT(), d.getTotalTVA(), d.getTotalTTC());
    }

    public LigneDto ligneToDto(LigneDevis l) {
        Long produitId = l.getProduit() == null ? null : l.getProduit().getId();
        return new LigneDto(l.getId(), produitId, l.getDesignation(), l.getQuantite(), l.getPrixUnitaireHT(),
                l.getRemise(), l.getTauxTVA(), calculateur.montantHT(l), calculateur.montantTVA(l));
    }

    /**
     * Crée une ligne de devis. Ce que le client n'a pas précisé est repris du produit du catalogue
     * (désignation, prix, TVA). Sans produit, désignation, prix et TVA sont obligatoires.
     *
     * @param produit le produit choisi, ou null pour une ligne libre
     */
    public LigneDevis versLigne(LigneDto dto, Produit produit) {
        String designation = dto.designation();
        if (designation == null || designation.isBlank()) {
            designation = produit == null ? null : produit.getDesignation();
        }
        BigDecimal prix = dto.prixUnitaireHT();
        if (prix == null && produit != null) {
            prix = produit.getPrixUnitaireHT();
        }
        BigDecimal taux = dto.tauxTVA();
        if (taux == null && produit != null) {
            taux = produit.getTauxTVA().getTaux();
        }
        if (designation == null || prix == null || taux == null) {
            throw new RegleMetierException(
                    "Une ligne sans produit doit avoir une désignation, un prix unitaire HT et un taux de TVA.");
        }

        LigneDevis ligne = new LigneDevis();
        ligne.setProduit(produit);
        ligne.setDesignation(designation);
        ligne.setQuantite(dto.quantite());
        ligne.setPrixUnitaireHT(prix);
        ligne.setRemise(dto.remise() == null ? BigDecimal.ZERO : dto.remise());
        ligne.setTauxTVA(taux);
        return ligne;
    }
}

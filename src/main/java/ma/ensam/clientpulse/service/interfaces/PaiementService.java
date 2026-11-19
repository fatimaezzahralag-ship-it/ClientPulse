package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.PaiementDto;

import java.util.List;

public interface PaiementService {

    /** Enregistre un paiement (partiel ou total) et renvoie la facture mise à jour. */
    FactureDto enregistrer(Long factureId, PaiementDto paiement);

    List<PaiementDto> lister(Long factureId);
}

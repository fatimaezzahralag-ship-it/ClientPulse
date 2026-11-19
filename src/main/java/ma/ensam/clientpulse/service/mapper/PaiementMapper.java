package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Paiement;
import ma.ensam.clientpulse.dto.PaiementDto;
import org.springframework.stereotype.Component;

@Component
public class PaiementMapper {

    public PaiementDto toDto(Paiement p) {
        return new PaiementDto(p.getId(), p.getFacture().getId(), p.getDatePaiement(),
                p.getMontant(), p.getMode(), p.getReference());
    }

    /** Crée un Paiement à partir du DTO. Le lien vers la facture est posé par Facture.enregistrerPaiement. */
    public Paiement versEntite(PaiementDto dto) {
        Paiement p = new Paiement();
        p.setDatePaiement(dto.datePaiement());
        p.setMontant(dto.montant());
        p.setMode(dto.mode());
        p.setReference(dto.reference());
        return p;
    }
}

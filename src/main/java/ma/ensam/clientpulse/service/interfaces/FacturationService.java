package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.dto.FactureDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface FacturationService {

    Page<FactureDto> lister(StatutFacture statut, Pageable pageable);

    FactureDto trouver(Long id);

    FactureDto creerDepuisDevis(Long devisId);

    FactureDto emettre(Long factureId, LocalDate dateEmission, LocalDate dateEcheance);

    FactureDto annuler(Long factureId);

    int marquerEnRetard(LocalDate jour);
}

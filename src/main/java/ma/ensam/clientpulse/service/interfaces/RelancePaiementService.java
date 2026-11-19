package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.ResultatTraitement;

import java.time.LocalDate;

public interface RelancePaiementService {


    int genererRelances(LocalDate jour);

    ResultatTraitement traiterJour(LocalDate jour);
}

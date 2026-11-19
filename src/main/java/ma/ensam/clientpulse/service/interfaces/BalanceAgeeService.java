package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.BalanceAgeeDto;

import java.time.LocalDate;
import java.util.List;

public interface BalanceAgeeService {

    /** Une ligne par client qui doit encore de l'argent, classée par tranche de retard à la date "jour". */
    List<BalanceAgeeDto> calculer(LocalDate jour);
}

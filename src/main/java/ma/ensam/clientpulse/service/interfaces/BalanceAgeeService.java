package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.BalanceAgeeDto;

import java.time.LocalDate;
import java.util.List;

public interface BalanceAgeeService {

    List<BalanceAgeeDto> calculer(LocalDate jour);
}

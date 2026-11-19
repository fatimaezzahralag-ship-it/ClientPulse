package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.CommercialDto;

import java.util.List;

public interface CommercialService {

    List<CommercialDto> findAll();
}

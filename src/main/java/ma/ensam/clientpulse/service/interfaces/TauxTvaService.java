package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.TauxTvaDto;

import java.util.List;

public interface TauxTvaService {

    List<TauxTvaDto> lister(boolean actifsSeulement);

    TauxTvaDto trouver(Long id);

    TauxTvaDto creer(TauxTvaDto dto);

    TauxTvaDto modifier(Long id, TauxTvaDto dto);

    TauxTvaDto desactiver(Long id);
}

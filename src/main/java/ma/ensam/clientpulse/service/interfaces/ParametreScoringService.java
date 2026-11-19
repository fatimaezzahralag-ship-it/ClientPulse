package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.ParametreScoringDto;
import ma.ensam.clientpulse.dto.ParametreScoringRequest;

import java.util.List;

/** Consultation et modification contrôlée des paramètres du scoring. */
public interface ParametreScoringService {

    List<ParametreScoringDto> findAll();

    ParametreScoringDto modifier(Long id, ParametreScoringRequest request);
}

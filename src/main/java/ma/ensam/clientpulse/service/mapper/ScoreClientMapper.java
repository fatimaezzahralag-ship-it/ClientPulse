package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.dto.ScoreClientDto;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScoreClientMapper {

    public ScoreClientDto toDto(ScoreClient score) {

        List<String> explications = score.getFacteursExplicatifs() == null
                ? List.of()
                : score.getFacteursExplicatifs()
                        .lines()
                        .filter(ligne -> !ligne.isBlank())
                        .toList();

        return new ScoreClientDto(
                score.getId(),
                score.getClient() != null
                        ? score.getClient().getId()
                        : null,
                score.getDateCalcul(),
                score.getScoreRisque(),
                score.getNiveau(),
                score.getSegment(),
                explications
        );
    }
}

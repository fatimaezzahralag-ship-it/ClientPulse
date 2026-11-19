package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.dto.ScoreClientDto;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.FacteurRisque;
import ma.ensam.clientpulse.service.ResultatFacteur;
import ma.ensam.clientpulse.service.facteur.BaisseAchatsFacteur;
import ma.ensam.clientpulse.service.facteur.InactiviteFacteur;
import ma.ensam.clientpulse.service.facteur.ReclamationsFacteur;
import ma.ensam.clientpulse.service.facteur.RetardPaiementFacteur;
import ma.ensam.clientpulse.service.interfaces.ChurnScoringService;
import ma.ensam.clientpulse.service.interfaces.RfmService;
import ma.ensam.clientpulse.service.mapper.ScoreClientMapper;
import ma.ensam.clientpulse.service.scoring.NomsParametres;
import ma.ensam.clientpulse.service.scoring.ParametresScoringReader;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Orchestre les facteurs de risque.
 *
 * Le score est une moyenne pondérée des notes des facteurs. Les poids, les
 * seuils de niveau et les seuils internes des facteurs viennent tous de la
 * table {@code parametres_scoring} : aucun n'est codé en dur dans le calcul.
 */
@Service
@Transactional
public class ChurnScoringServiceImpl implements ChurnScoringService {

    private final List<FacteurRisque> facteurs;
    private final ParametresScoringReader parametres;
    private final ScoreClientDao scoreClientDao;
    private final ClientDao clientDao;
    private final RfmService rfmService;
    private final ScoreClientMapper scoreClientMapper;

    public ChurnScoringServiceImpl(
            List<FacteurRisque> facteurs,
            ParametresScoringReader parametres,
            ScoreClientDao scoreClientDao,
            ClientDao clientDao,
            RfmService rfmService,
            ScoreClientMapper scoreClientMapper) {
        this.facteurs = facteurs;
        this.parametres = parametres;
        this.scoreClientDao = scoreClientDao;
        this.clientDao = clientDao;
        this.rfmService = rfmService;
        this.scoreClientMapper = scoreClientMapper;
    }

    @Override
    public ScoreClient calculer(Client client, LocalDate jour) {

        List<ResultatFacteur> resultats = facteurs.stream()
                .map(facteur -> facteur.evaluer(client, jour))
                .toList();

        BigDecimal score = BigDecimal.ZERO;

        for (ResultatFacteur resultat : resultats) {
            BigDecimal note = resultat.note() != null
                    ? resultat.note()
                    : BigDecimal.ZERO;
            BigDecimal poids = parametres.valeur(
                    nomDuPoids(resultat.code()));
            score = score.add(note.multiply(poids));
        }

        score = score
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO)
                .min(BigDecimal.valueOf(100));

        BigDecimal seuilMoyen = parametres.valeur(
                NomsParametres.SEUIL_RISQUE_MOYEN);
        BigDecimal seuilEleve = parametres.valeur(
                NomsParametres.SEUIL_RISQUE_ELEVE);
        BigDecimal seuilCritique = parametres.valeur(
                NomsParametres.SEUIL_RISQUE_CRITIQUE);

        NiveauRisque niveau = determinerNiveau(
                score, seuilMoyen, seuilEleve, seuilCritique);

        // Segmentation RFM déléguée : une seule source de vérité pour le
        // segment, partagée avec le job nocturne et les relances commerciales.
        var segment = rfmService.segmenter(client, jour);

        ScoreClient resultat = new ScoreClient();
        resultat.setClient(client);
        resultat.setDateCalcul(jour);
        resultat.setScoreRisque(score);
        resultat.setNiveau(niveau);
        resultat.setSegment(segment);
        resultat.setFacteursExplicatifs(
                resultats.stream()
                        .map(ResultatFacteur::explication)
                        .collect(Collectors.joining("\n"))
        );

        return scoreClientDao.save(resultat);
    }

    @Override
    public ScoreClientDto calculerPourClient(Long clientId) {
        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Client introuvable : " + clientId));
        return scoreClientMapper.toDto(calculer(client, LocalDate.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScoreClientDto> dernierScore(Long clientId) {
        return scoreClientDao.findDernierByClientId(clientId)
                .map(scoreClientMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoreClientDto> historique(Long clientId) {
        return scoreClientDao.findHistoriqueByClientId(clientId).stream()
                .map(scoreClientMapper::toDto)
                .toList();
    }

    private static String nomDuPoids(String codeFacteur) {
        return switch (codeFacteur) {
            case InactiviteFacteur.CODE -> NomsParametres.POIDS_INACTIVITE;
            case BaisseAchatsFacteur.CODE -> NomsParametres.POIDS_BAISSE_ACHATS;
            case ReclamationsFacteur.CODE -> NomsParametres.POIDS_RECLAMATIONS;
            case RetardPaiementFacteur.CODE ->
                    NomsParametres.POIDS_RETARD_PAIEMENT;
            default -> throw new IllegalStateException(
                    "Aucun poids configuré pour le facteur : " + codeFacteur);
        };
    }

    private static NiveauRisque determinerNiveau(
            BigDecimal score,
            BigDecimal seuilMoyen,
            BigDecimal seuilEleve,
            BigDecimal seuilCritique) {

        if (score.compareTo(seuilCritique) >= 0) {
            return NiveauRisque.CRITIQUE;
        }
        if (score.compareTo(seuilEleve) >= 0) {
            return NiveauRisque.ELEVE;
        }
        if (score.compareTo(seuilMoyen) >= 0) {
            return NiveauRisque.MOYEN;
        }
        return NiveauRisque.FAIBLE;
    }

}

package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Résultat d'un calcul de risque de churn pour un client, à une date donnée.
 *
 * L'historique est conservé : chaque calcul produit une nouvelle ligne. Le score
 * le plus récent est celui qui est exposé par l'API.
 */
@Entity
@Table(
        name = "scores_client",
        indexes = {
                @Index(
                        name = "idx_scores_client_client_date",
                        columnList = "client_id, date_calcul"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ScoreClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "date_calcul", nullable = false)
    private LocalDate dateCalcul;

    /** Score de risque normalisé entre 0 (aucun risque) et 100 (risque maximal). */
    @Column(name = "score_risque", nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreRisque;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", nullable = false, length = 20)
    private NiveauRisque niveau;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment", nullable = false, length = 20)
    private SegmentRFM segment;

    /** Une explication par ligne, telle qu'affichée à l'utilisateur. */
    @Column(name = "facteurs_explicatifs", columnDefinition = "TEXT")
    private String facteursExplicatifs;
}

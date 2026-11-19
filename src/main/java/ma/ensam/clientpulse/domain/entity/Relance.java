package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.ResultatRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;

import java.time.LocalDate;

/**
 * Relance client : nature PAIEMENT (P1) ou COMMERCIALE (P2).
 *
 * C'est l'unique entité Relance du projet. Le contrat commun est exposé par
 * {@code RelanceService} ; P2 n'utilise que la nature COMMERCIALE.
 */
@Entity
@Table(
        name = "relances",
        indexes = {
                @Index(
                        name = "idx_relances_client_nature_statut",
                        columnList = "client_id, nature, statut"
                ),
                @Index(
                        name = "idx_relances_commercial_statut",
                        columnList = "commercial_id, statut"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Relance extends EntiteTracable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commercial_id")
    private Commercial commercial;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature", nullable = false, length = 20)
    private NatureRelance nature;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutRelance statut;

    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat", length = 30)
    private ResultatRelance resultat;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
}

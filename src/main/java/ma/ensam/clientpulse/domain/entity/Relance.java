package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "relance")
@Getter
@Setter
@NoArgsConstructor
public class Relance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature", nullable = false, length = 20)
    private NatureRelance nature;

    @Column(name = "niveau", nullable = false)
    private int niveau;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "echeance")
    private LocalDate echeance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutRelance statut = StatutRelance.A_FAIRE;

    @Column(name = "resultat", length = 255)
    private String resultat;

    @Column(name = "contenu", length = 1000)
    private String contenu;

    @Column(name = "suggeree_par_systeme", nullable = false)
    private boolean suggereeParSysteme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @Column(name = "affecte_a_id")
    private Long affecteAId;
}

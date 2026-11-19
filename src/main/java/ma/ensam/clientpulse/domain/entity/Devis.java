package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.ensam.clientpulse.domain.enums.StatutDevis;
import ma.ensam.clientpulse.exception.RegleMetierException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Proposition commerciale envoyée à un client. */
@Entity
@Table(name = "devis")
@Getter
@Setter
@NoArgsConstructor
public class Devis extends EntiteTracable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "date_emission", nullable = false)
    private LocalDate dateEmission;

    @Column(name = "date_validite")
    private LocalDate dateValidite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutDevis statut = StatutDevis.BROUILLON;

    @Column(name = "total_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHT = BigDecimal.ZERO;

    @Column(name = "total_tva", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalTVA = BigDecimal.ZERO;

    @Column(name = "total_ttc", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalTTC = BigDecimal.ZERO;

    // LAZY : le client n'est chargé que si on l'utilise (évite des requêtes inutiles)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commercial_id")
    private Commercial commercial;

    // Composition : les lignes vivent et meurent avec le devis (cascade + orphanRemoval)
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneDevis> lignes = new ArrayList<>();

    /** Ajoute une ligne ET règle le lien dans les deux sens (sinon devis_id reste vide en base). */
    public void ajouterLigne(LigneDevis ligne) {
        lignes.add(ligne);
        ligne.setDevis(this);
    }

    /** Vrai si la date de validité est dépassée. */
    public boolean estExpire(LocalDate jour) {
        return dateValidite != null && jour.isAfter(dateValidite);
    }

    // ------------------------------------------------------------------ règles métier

    /** Seul un brouillon peut être modifié ou supprimé. */
    public boolean estModifiable() {
        return statut == StatutDevis.BROUILLON;
    }

    /** BROUILLON -> ENVOYE */
    public void envoyer() {
        if (statut != StatutDevis.BROUILLON) {
            throw new RegleMetierException("Seul un brouillon peut être envoyé (statut actuel : " + statut + ").");
        }
        if (lignes.isEmpty()) {
            throw new RegleMetierException("Impossible d'envoyer un devis sans ligne.");
        }
        statut = StatutDevis.ENVOYE;
    }

    /** ENVOYE -> ACCEPTE, à condition que le devis ne soit pas expiré. */
    public void accepter(LocalDate jour) {
        if (statut != StatutDevis.ENVOYE) {
            throw new RegleMetierException("Seul un devis envoyé peut être accepté (statut actuel : " + statut + ").");
        }
        if (estExpire(jour)) {
            throw new RegleMetierException("Ce devis est expiré depuis le " + dateValidite + ".");
        }
        statut = StatutDevis.ACCEPTE;
    }

    /** ENVOYE -> REFUSE */
    public void refuser() {
        if (statut != StatutDevis.ENVOYE) {
            throw new RegleMetierException("Seul un devis envoyé peut être refusé (statut actuel : " + statut + ").");
        }
        statut = StatutDevis.REFUSE;
    }
}

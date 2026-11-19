package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeFacture;
import ma.ensam.clientpulse.exception.RegleMetierException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Document comptable : une facture, ou un avoir qui l'annule.
 * Les règles simples (émettre, payer, annuler) sont écrites ICI, pas dans les contrôleurs.
 */
@Entity
@Table(name = "facture")
@Getter
@Setter
@NoArgsConstructor
public class Facture extends EntiteTracable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Vide tant que la facture est en brouillon. Rempli à l'émission (FAC-AAAA-NNNN). */
    @Column(name = "numero", unique = true, length = 30)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TypeFacture type = TypeFacture.FACTURE;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutFacture statut = StatutFacture.BROUILLON;

    @Column(name = "total_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHT = BigDecimal.ZERO;

    @Column(name = "total_tva", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalTVA = BigDecimal.ZERO;

    @Column(name = "total_ttc", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalTTC = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Le devis d'origine (facultatif). Une facture au plus par devis : UNIQUE en base.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", unique = true)
    private Devis devis;

    // Pour un avoir : la facture qu'il corrige (association réflexive)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_origine_id")
    private Facture factureOrigine;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    private List<Paiement> paiements = new ArrayList<>();

    public void ajouterLigne(LigneFacture ligne) {
        lignes.add(ligne);
        ligne.setFacture(this);
    }

    // ------------------------------------------------------------------ règles métier

    /** Seul un brouillon peut être modifié. Une facture émise est figée. */
    public boolean estModifiable() {
        return statut == StatutFacture.BROUILLON;
    }

    /** Somme des paiements déjà reçus. */
    public BigDecimal getTotalPaye() {
        BigDecimal somme = BigDecimal.ZERO;
        for (Paiement p : paiements) {
            somme = somme.add(p.getMontant());
        }
        return somme;
    }

    /**
     * Reste à payer = total TTC - paiements. Valeur CALCULÉE : on ne la stocke pas en base
     * (comme le dit le Livrable 2), donc elle ne peut jamais être fausse.
     */
    public BigDecimal getResteAPayer() {
        return totalTTC.subtract(getTotalPaye());
    }

    /** Donne le numéro à la facture et la fige. */
    public void emettre(String numero) {
        if (!estModifiable()) {
            throw new RegleMetierException("Cette facture est déjà émise.");
        }
        if (lignes.isEmpty()) {
            throw new RegleMetierException("Impossible d'émettre une facture sans ligne.");
        }
        this.numero = numero;
        this.statut = StatutFacture.EMISE;
    }

    /** Ajoute un paiement (partiel ou total) puis recalcule le statut. */
    public void enregistrerPaiement(Paiement paiement) {
        boolean payable = statut == StatutFacture.EMISE
                || statut == StatutFacture.PARTIELLEMENT_PAYEE
                || statut == StatutFacture.EN_RETARD;
        if (!payable) {
            throw new RegleMetierException("On ne peut pas payer une facture au statut " + statut + ".");
        }
        if (paiement.getMontant() == null || paiement.getMontant().signum() <= 0) {
            throw new RegleMetierException("Le montant du paiement doit être positif.");
        }
        if (paiement.getMontant().compareTo(getResteAPayer()) > 0) {
            throw new RegleMetierException("Le paiement dépasse le reste à payer.");
        }
        paiements.add(paiement);
        paiement.setFacture(this);

        // Reste = 0 -> PAYEE, sinon PARTIELLEMENT_PAYEE
        if (getResteAPayer().signum() == 0) {
            statut = StatutFacture.PAYEE;
        } else {
            statut = StatutFacture.PARTIELLEMENT_PAYEE;
        }
    }

    /** Annule une facture émise qui n'a reçu aucun paiement. */
    public void annuler() {
        if (statut == StatutFacture.BROUILLON) {
            throw new RegleMetierException("Un brouillon se supprime, il ne s'annule pas.");
        }
        if (statut == StatutFacture.ANNULEE) {
            throw new RegleMetierException("Cette facture est déjà annulée.");
        }
        if (!paiements.isEmpty()) {
            throw new RegleMetierException("Cette facture a déjà reçu des paiements.");
        }
        statut = StatutFacture.ANNULEE;
    }

    /** Échéance dépassée ET encore de l'argent à recevoir ? (utilisé par la tâche nocturne en J4) */
    public boolean estEnRetard(LocalDate jour) {
        boolean ouverte = statut == StatutFacture.EMISE
                || statut == StatutFacture.PARTIELLEMENT_PAYEE
                || statut == StatutFacture.EN_RETARD;
        return ouverte
                && dateEcheance != null
                && jour.isAfter(dateEcheance)
                && getResteAPayer().signum() > 0;
    }

    /**
     * Passe la facture en EN_RETARD si l'échéance est dépassée (appelé chaque nuit par le job).
     * Une facture déjà EN_RETARD n'est pas modifiée.
     * @return vrai si le statut vient de changer
     */
    public boolean marquerEnRetard(LocalDate jour) {
        boolean candidate = statut == StatutFacture.EMISE || statut == StatutFacture.PARTIELLEMENT_PAYEE;
        if (candidate && estEnRetard(jour)) {
            statut = StatutFacture.EN_RETARD;
            return true;
        }
        return false;
    }
}

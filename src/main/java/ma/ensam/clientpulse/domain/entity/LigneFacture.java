package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Une ligne de facture. Le taux de TVA est COPIÉ à l'émission : si le taux change plus tard,
 * les anciennes factures ne bougent pas. Le lien vers Produit sera ajouté en J3.
 */
@Entity
@Table(name = "ligne_facture")
@Getter
@Setter
@NoArgsConstructor
public class LigneFacture implements LigneCalculable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @Column(name = "designation", nullable = false, length = 255)
    private String designation;

    @Column(name = "quantite", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaireHT;

    @Column(name = "remise", precision = 5, scale = 2)
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTVA;

    // Montants de la ligne, calculés par CalculateurMontants puis stockés
    @Column(name = "montant_ht", precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(name = "montant_tva", precision = 15, scale = 2)
    private BigDecimal montantTVA;
}

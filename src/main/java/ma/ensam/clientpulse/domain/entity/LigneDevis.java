package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Une ligne d'un devis. Le lien vers Produit sera ajouté en J3 (catalogue).
 */
@Entity
@Table(name = "ligne_devis")
@Getter
@Setter
@NoArgsConstructor
public class LigneDevis implements LigneCalculable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;

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
}

package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "produit")
@Getter
@Setter
@NoArgsConstructor
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "designation", nullable = false, length = 255)
    private String designation;

    @Column(name = "prix_unitaire_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaireHT;

    @Column(name = "marge", precision = 5, scale = 2)
    private BigDecimal marge;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "taux_tva_id", nullable = false)
    private TauxTVA tauxTVA;
}

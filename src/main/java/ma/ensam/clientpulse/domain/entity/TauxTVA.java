package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Un taux de TVA paramétrable (20 %, 14 %, 10 %, 7 %, 0 %).
 * On ne supprime jamais un taux déjà utilisé : on le désactive (actif = false).
 */
@Entity
@Table(name = "taux_tva")
@Getter
@Setter
@NoArgsConstructor
public class TauxTVA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", nullable = false, length = 50)
    private String libelle;

    /** En pourcentage : 20 veut dire 20 %. */
    @Column(name = "taux", nullable = false, precision = 5, scale = 2)
    private BigDecimal taux;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}

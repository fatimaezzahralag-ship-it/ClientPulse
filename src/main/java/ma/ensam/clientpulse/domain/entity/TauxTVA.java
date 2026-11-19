package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    @Column(name = "taux", nullable = false, precision = 5, scale = 2)
    private BigDecimal taux;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}

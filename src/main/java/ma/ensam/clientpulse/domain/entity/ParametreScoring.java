package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Paramètre du moteur de scoring (poids d'un facteur, seuil de risque, ...).
 *
 * Les valeurs sont stockées en base pour être modifiables sans recompiler
 * l'application : c'est la raison d'être de cette table.
 */
@Entity
@Table(name = "parametres_scoring")
@Getter
@Setter
@NoArgsConstructor
public class ParametreScoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;

    @Column(name = "valeur", nullable = false, precision = 15, scale = 4)
    private BigDecimal valeur;

    public ParametreScoring(String nom, BigDecimal valeur) {
        this.nom = nom;
        this.valeur = valeur;
    }
}

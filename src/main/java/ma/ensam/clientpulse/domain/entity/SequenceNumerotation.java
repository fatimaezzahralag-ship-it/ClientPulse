package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.ensam.clientpulse.domain.enums.TypeDocument;

/**
 * Compteur de numérotation : UNE ligne par (type de document, année).
 * Exemple : (FACTURE, 2026, dernierNumero = 41) -> la prochaine facture sera FAC-2026-0042.
 */
@Entity
@Table(name = "sequence_numerotation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"type", "annee"}))
@Getter
@Setter
@NoArgsConstructor
public class SequenceNumerotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TypeDocument type;

    @Column(name = "annee", nullable = false)
    private int annee;

    @Column(name = "dernier_numero", nullable = false)
    private int dernierNumero;

    public SequenceNumerotation(TypeDocument type, int annee, int dernierNumero) {
        this.type = type;
        this.annee = annee;
        this.dernierNumero = dernierNumero;
    }

    /** Avance le compteur de 1 et renvoie le nouveau numéro. */
    public int incrementer() {
        dernierNumero++;
        return dernierNumero;
    }
}

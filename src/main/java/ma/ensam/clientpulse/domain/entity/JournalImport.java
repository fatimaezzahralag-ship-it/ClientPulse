package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Trace d'un import CSV (table {@code journal_import}, migration V7).
 * La date et l'auteur viennent de {@link EntiteTracable} (remplis par AuditConfig).
 */
@Entity
@Table(name = "journal_import")
@Getter
@Setter
@NoArgsConstructor
public class JournalImport extends EntiteTracable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fichier", nullable = false, length = 255)
    private String fichier;

    @Column(name = "lignes_lues", nullable = false)
    private int lignesLues;

    @Column(name = "lignes_importees", nullable = false)
    private int lignesImportees;

    @Column(name = "lignes_rejetees", nullable = false)
    private int lignesRejetees;
}

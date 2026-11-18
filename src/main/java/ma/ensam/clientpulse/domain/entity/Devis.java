package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.ensam.clientpulse.domain.enums.StatutDevis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneDevis> lignes = new ArrayList<>();

    public void ajouterLigne(LigneDevis ligne) {
        lignes.add(ligne);
        ligne.setDevis(this);
    }

    public boolean estExpire(LocalDate jour) {
        return dateValidite != null && jour.isAfter(dateValidite);
    }
}

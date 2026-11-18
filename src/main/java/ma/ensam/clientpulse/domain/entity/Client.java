package ma.ensam.clientpulse.domain.entity;

import ma.ensam.clientpulse.domain.enums.TypeClient;
import ma.ensam.clientpulse.domain.enums.StatutClient;

import jakarta.persistence.*;

@Entity
@Table(
        name = "clients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "code")
        }
)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String raisonSociale;

    @Column(length = 20)
    private String ice;

    private String email;

    private String telephone;

    private String adresse;

    private String ville;

    private String secteur;

    private Integer delaiPaiementJours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeClient type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutClient statut;

    @ManyToOne
    @JoinColumn(name = "commercial_id")
    private Commercial commercial;

    public Client() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getIce() {
        return ice;
    }

    public void setIce(String ice) {
        this.ice = ice;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public Integer getDelaiPaiementJours() {
        return delaiPaiementJours;
    }

    public void setDelaiPaiementJours(Integer delaiPaiementJours) {
        this.delaiPaiementJours = delaiPaiementJours;
    }

    public TypeClient getType() {
        return type;
    }

    public void setType(TypeClient type) {
        this.type = type;
    }

    public StatutClient getStatut() {
        return statut;
    }

    public void setStatut(StatutClient statut) {
        this.statut = statut;
    }

    public Commercial getCommercial() {
        return commercial;
    }

    public void setCommercial(Commercial commercial) {
        this.commercial = commercial;
    }
}
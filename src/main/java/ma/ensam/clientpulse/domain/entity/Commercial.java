package ma.ensam.clientpulse.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commerciaux")
public class Commercial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(
            name = "utilisateur_id",
            nullable = false,
            unique = true
    )
    private Utilisateur utilisateur;

    private String equipe;

    @Column(precision = 12, scale = 2)
    private BigDecimal objectifMensuel;

    @OneToMany(mappedBy = "commercial")
    private List<Client> clients = new ArrayList<>();

    public Commercial() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getEquipe() {
        return equipe;
    }

    public void setEquipe(String equipe) {
        this.equipe = equipe;
    }

    public BigDecimal getObjectifMensuel() {
        return objectifMensuel;
    }

    public void setObjectifMensuel(BigDecimal objectifMensuel) {
        this.objectifMensuel = objectifMensuel;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }
}
package ma.ensam.clientpulse.domain.entity;

import ma.ensam.clientpulse.domain.enums.TypeInteraction;
import ma.ensam.clientpulse.domain.enums.StatutInteraction;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeInteraction type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 2000)
    private String resume;

    @Enumerated(EnumType.STRING)
    private StatutInteraction statut;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    public Interaction() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeInteraction getType() {
        return type;
    }

    public void setType(TypeInteraction type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public StatutInteraction getStatut() {
        return statut;
    }

    public void setStatut(StatutInteraction statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
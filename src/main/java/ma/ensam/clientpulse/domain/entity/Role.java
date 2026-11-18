package ma.ensam.clientpulse.domain.entity;

import ma.ensam.clientpulse.domain.enums.RoleNom;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleNom nom;

    public Role() {}

    public Role(RoleNom nom) {
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public RoleNom getNom() {
        return nom;
    }

    public void setNom(RoleNom nom) {
        this.nom = nom;
    }
}
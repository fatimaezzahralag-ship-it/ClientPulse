package ma.ensam.clientpulse.config;

import ma.ensam.clientpulse.dao.interfaces.CommercialDao;
import ma.ensam.clientpulse.dao.interfaces.RoleDao;
import ma.ensam.clientpulse.dao.interfaces.UtilisateurDao;
import ma.ensam.clientpulse.domain.entity.Commercial;
import ma.ensam.clientpulse.domain.entity.Role;
import ma.ensam.clientpulse.domain.entity.Utilisateur;
import ma.ensam.clientpulse.domain.enums.RoleNom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleDao roleDao;
    private final UtilisateurDao utilisateurDao;
    private final CommercialDao commercialDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${spring.flyway.enabled:false}")
    private boolean flywayEnabled;

    @Value("${app.seed.admin.email:admin@clientpulse.ma}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin@12345}")
    private String adminPassword;

    @Value("${app.seed.commercial.email:commercial@clientpulse.ma}")
    private String commercialEmail;

    @Value("${app.seed.commercial.password:Commercial@12345}")
    private String commercialPassword;

    public DataInitializer(
            RoleDao roleDao,
            UtilisateurDao utilisateurDao,
            CommercialDao commercialDao,
            PasswordEncoder passwordEncoder) {
        this.roleDao = roleDao;
        this.utilisateurDao = utilisateurDao;
        this.commercialDao = commercialDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            return;
        }

        // Les rôles sont créés par V2__roles.sql dès que Flyway est actif.
        // Tant que Flyway est désactivé, on les insère ici de façon idempotente.
        if (!flywayEnabled) {
            for (RoleNom nom : RoleNom.values()) {
                if (roleDao.findByNom(nom).isEmpty()) {
                    roleDao.save(new Role(nom));
                }
            }
        }

        creerUtilisateurSiAbsent(
                adminEmail, "Admin", "ClientPulse",
                RoleNom.ADMIN, adminPassword
        );

        Utilisateur commercial = creerUtilisateurSiAbsent(
                commercialEmail, "Commercial", "Demo",
                RoleNom.COMMERCIAL, commercialPassword
        );

        if (commercialDao.findByUtilisateurId(commercial.getId()).isEmpty()) {
            Commercial profil = new Commercial();
            profil.setUtilisateur(commercial);
            profil.setEquipe("Équipe commerciale");
            commercialDao.save(profil);
        }
    }

    private Utilisateur creerUtilisateurSiAbsent(
            String email,
            String prenom,
            String nom,
            RoleNom roleNom,
            String motDePasse) {

        String emailNormalise = email.trim().toLowerCase();

        Role role = roleDao.findByNom(roleNom)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Rôle introuvable : " + roleNom
                        )
                );

        Utilisateur utilisateur = utilisateurDao
                .findByEmail(emailNormalise)
                .orElseGet(() -> {
                    Utilisateur nouveau = new Utilisateur();
                    nouveau.setNom(nom);
                    nouveau.setPrenom(prenom);
                    nouveau.setEmail(emailNormalise);
                    nouveau.setMotDePasse(
                            passwordEncoder.encode(motDePasse)
                    );
                    nouveau.setActif(true);
                    return nouveau;
                });

        boolean possedeRole = utilisateur.getRoles()
                .stream()
                .anyMatch(r -> r.getNom() == roleNom);

        if (!possedeRole) {
            utilisateur.getRoles().add(role);
        }

        return utilisateurDao.save(utilisateur);
    }
}

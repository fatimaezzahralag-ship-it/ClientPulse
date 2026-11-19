package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import ma.ensam.clientpulse.domain.enums.RoleNom;

import java.util.Set;

/** Modification d'un compte : le mot de passe reste facultatif. */
public record UtilisateurModificationRequest(

        @NotBlank String nom,

        @NotBlank String prenom,

        @NotBlank @Email String email,

        @Size(min = 8) String motDePasse,

        @NotEmpty Set<RoleNom> roles

) {}

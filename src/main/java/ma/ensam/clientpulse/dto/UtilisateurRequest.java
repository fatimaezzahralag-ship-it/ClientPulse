package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

import ma.ensam.clientpulse.domain.enums.RoleNom;

public record UtilisateurRequest(

    @NotBlank
    String nom,

    @NotBlank
    String prenom,

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8)
    String motDePasse,

    @NotEmpty
    Set<RoleNom> roles

) {}

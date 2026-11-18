package ma.ensam.clientpulse.dto;

import java.util.Set;

public record UtilisateurDto(
    Long id,
    String nom,
    String prenom,
    String email,
    boolean actif,
    Set<String> roles
) {}

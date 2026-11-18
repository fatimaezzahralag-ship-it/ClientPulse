package ma.ensam.clientpulse.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dao.interfaces.UtilisateurDao;
import ma.ensam.clientpulse.domain.entity.Utilisateur;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurDao utilisateurDao;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

        String[] authorities = utilisateur.getRoles().stream()
                .map(role -> "ROLE_" + role.getNom().name())
                .toArray(String[]::new);

        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .disabled(!utilisateur.isActif())
                .authorities(authorities)
                .build();
    }
}

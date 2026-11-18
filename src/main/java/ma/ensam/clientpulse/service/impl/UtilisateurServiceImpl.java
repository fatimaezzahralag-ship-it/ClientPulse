package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.RoleDao;
import ma.ensam.clientpulse.dao.interfaces.UtilisateurDao;
import ma.ensam.clientpulse.domain.entity.Role;
import ma.ensam.clientpulse.domain.entity.Utilisateur;
import ma.ensam.clientpulse.dto.UtilisateurDto;
import ma.ensam.clientpulse.dto.UtilisateurRequest;
import ma.ensam.clientpulse.exception.DoublonException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.UtilisateurService;
import ma.ensam.clientpulse.service.mapper.UtilisateurMapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurDao utilisateurDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurMapper utilisateurMapper;

    public UtilisateurServiceImpl(
            UtilisateurDao utilisateurDao,
            RoleDao roleDao,
            PasswordEncoder passwordEncoder,
            UtilisateurMapper utilisateurMapper) {
        this.utilisateurDao = utilisateurDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurDto> findAll() {

        return utilisateurDao.findAll()
            .stream()
            .map(utilisateurMapper::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto findById(Long id) {

        Utilisateur utilisateur = utilisateurDao.findById(id)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Utilisateur introuvable : " + id
                )
            );

        return utilisateurMapper.toDto(utilisateur);
    }

    @Override
    public UtilisateurDto create(UtilisateurRequest request) {

        String email = request.email().trim().toLowerCase();

        if (utilisateurDao.existsByEmail(email)) {
            throw new DoublonException(
                "Un utilisateur avec cet email existe déjà"
            );
        }

        Set<Role> roles = request.roles()
            .stream()
            .map(roleNom -> roleDao.findByNom(roleNom)
                .orElseThrow(() ->
                    new RessourceIntrouvableException(
                        "Rôle introuvable : " + roleNom
                    )
                )
            )
            .collect(Collectors.toSet());

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(
            passwordEncoder.encode(request.motDePasse())
        );
        utilisateur.setActif(true);
        utilisateur.setRoles(roles);

        return utilisateurMapper.toDto(
            utilisateurDao.save(utilisateur)
        );
    }

    @Override
    public void deactivate(Long id) {

        Utilisateur utilisateur = utilisateurDao.findById(id)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Utilisateur introuvable : " + id
                )
            );

        utilisateur.setActif(false);
        utilisateurDao.save(utilisateur);
    }
}

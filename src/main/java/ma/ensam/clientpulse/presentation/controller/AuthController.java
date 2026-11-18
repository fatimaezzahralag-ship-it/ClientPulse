package ma.ensam.clientpulse.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import ma.ensam.clientpulse.dto.LoginRequest;
import ma.ensam.clientpulse.dto.UtilisateurDto;
import ma.ensam.clientpulse.service.interfaces.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository contextRepository;

    public AuthController(
            AuthService authService,
            SecurityContextRepository contextRepository) {
        this.authService = authService;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UtilisateurDto> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Authentication authentication =
            authService.authenticate(request);

        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        contextRepository.saveContext(
            context,
            httpRequest,
            httpResponse
        );

        UtilisateurDto utilisateur =
            authService.getUtilisateurConnecte(
                authentication.getName()
            );

        return ResponseEntity.ok(utilisateur);
    }
}

package ma.ensam.clientpulse.presentation.advice;

import lombok.extern.slf4j.Slf4j;
import ma.ensam.clientpulse.dto.ErreurApi;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Un seul endroit transforme toutes les exceptions en réponses JSON propres.
 * Les controllers et les services n'ont donc jamais à écrire de try/catch pour ça.
 * (401 et 403 sont gérés par la sécurité, dans SecurityConfig de P2.)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurApi> introuvable(RessourceIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(RegleMetierException.class)
    public ResponseEntity<ErreurApi> regleMetier(RegleMetierException e) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), null);
    }

    /** @Valid a refusé le corps de la requête : on liste chaque champ fautif. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException e) {
        Map<String, String> details = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erreur -> details.putIfAbsent(erreur.getField(), erreur.getDefaultMessage()));
        return reponse(HttpStatus.BAD_REQUEST, "Des champs sont invalides.", details);
    }

    /** Contrainte de la base violée (doublon d'une valeur unique, clé étrangère...). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException e) {
        log.warn("Contrainte de base violée : {}", e.getMostSpecificCause().getMessage());
        return reponse(HttpStatus.CONFLICT, "L'opération viole une contrainte de la base de données.", null);
    }

    /** Filet de sécurité : on ne montre jamais la trace d'une erreur inattendue au client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> inattendue(Exception e) {
        log.error("Erreur inattendue", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur.", null);
    }

    private ResponseEntity<ErreurApi> reponse(HttpStatus statut, String message, Map<String, String> details) {
        ErreurApi corps = new ErreurApi(LocalDateTime.now(), statut.value(), statut.getReasonPhrase(), message, details);
        return ResponseEntity.status(statut).body(corps);
    }
}

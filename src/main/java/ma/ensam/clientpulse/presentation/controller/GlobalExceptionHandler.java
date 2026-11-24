package ma.ensam.clientpulse.presentation.controller;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.ensam.clientpulse.exception.DoublonException;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ProblemDetail> gererRessourceIntrouvable(RessourceIntrouvableException ex) {
        return reponse(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage());
    }

    @ExceptionHandler(DoublonException.class)
    public ResponseEntity<ProblemDetail> gererDoublon(DoublonException ex) {
        return reponse(HttpStatus.CONFLICT, "Doublon", ex.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    public ResponseEntity<ProblemDetail> gererRegleMetier(RegleMetierException ex) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, "Règle métier non respectée", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> gererEntiteIntrouvable(EntityNotFoundException ex) {
        return reponse(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> gererRequeteInvalide(IllegalArgumentException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> gererAuthentification(AuthenticationException ex) {
        return reponse(HttpStatus.UNAUTHORIZED, "Authentification échouée", "Email ou mot de passe incorrect");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> gererValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(erreur -> erreur.getField() + " : " + erreur.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return reponse(HttpStatus.BAD_REQUEST, "Validation échouée", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> gererCorpsIllisible(HttpMessageNotReadableException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Le corps de la requête est invalide ou contient une valeur inconnue");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> gererParametreInvalide(MethodArgumentTypeMismatchException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Valeur invalide pour le paramètre : " + ex.getName());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> gererAccesRefuse(AccessDeniedException ex) {
        return reponse(HttpStatus.FORBIDDEN, "Accès refusé", "Votre rôle ne permet pas cette opération");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> gererFichierTropGros(MaxUploadSizeExceededException ex) {
        return reponse(HttpStatus.CONTENT_TOO_LARGE, "Fichier trop volumineux",
                "Le fichier dépasse la taille maximale autorisée (5 Mo)");
    }

    /** Dernier filet de sécurité : aucune trace technique n'est jamais renvoyée au navigateur. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> gererErreurInattendue(Exception ex) throws Exception {
        if (ex instanceof org.springframework.web.ErrorResponse) {
            throw ex;   // 404, 405, 415… : laissés à Spring, qui sait les traduire
        }
        LOG.error("Erreur inattendue", ex);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne",
                "Une erreur inattendue s'est produite. Réessayez ou contactez l'administrateur");
    }

    private ResponseEntity<ProblemDetail> reponse(HttpStatus status, String titre, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(titre);
        return ResponseEntity.status(status).body(problemDetail);
    }
}

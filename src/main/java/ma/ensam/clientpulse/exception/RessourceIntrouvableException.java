package ma.ensam.clientpulse.exception;

public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }

    /** Exemple : RessourceIntrouvableException.de("Facture", 12) donne "Facture introuvable (id 12)". */
    public static RessourceIntrouvableException de(String objet, Object id) {
        return new RessourceIntrouvableException(objet + " introuvable (id " + id + ")");
    }
}

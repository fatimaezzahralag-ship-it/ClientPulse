package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.JournalImportDto;
import ma.ensam.clientpulse.dto.RapportImportDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;

/**
 * Import de clients depuis un fichier CSV (exigence C8).
 *
 * Le service ne connaît pas HTTP : il reçoit un flux et un nom de fichier.
 */
public interface CsvImportService {

    /**
     * Contrôle, nettoie et importe les clients du fichier. Une ligne invalide est
     * rejetée et signalée dans le rapport, sans bloquer les autres lignes.
     *
     * @throws ma.ensam.clientpulse.exception.RegleMetierException si le fichier est
     *         vide, illisible, trop gros ou s'il lui manque une colonne obligatoire
     */
    RapportImportDto importerClients(String nomFichier, InputStream contenu);

    /** Historique des imports, du plus récent au plus ancien. */
    Page<JournalImportDto> historique(Pageable pageable);

    /** Fichier CSV d'exemple (en-têtes + une ligne) à télécharger par l'utilisateur. */
    String modeleClients();
}

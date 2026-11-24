package ma.ensam.clientpulse.presentation.controller;

import ma.ensam.clientpulse.dto.JournalImportDto;
import ma.ensam.clientpulse.dto.RapportImportDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.service.interfaces.CsvImportService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Import CSV de clients (ADMIN uniquement, voir SecurityConfig).
 *
 * POST /api/import/clients          fichier CSV (champ « fichier »), renvoie le rapport d'import
 * GET  /api/import/journal          historique des imports
 * GET  /api/import/modele-clients   fichier CSV d'exemple
 */
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final CsvImportService csvImportService;

    public ImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @PostMapping(value = "/clients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RapportImportDto importerClients(@RequestParam("fichier") MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new RegleMetierException("Aucun fichier reçu.");
        }
        String nom = fichier.getOriginalFilename() == null ? "" : fichier.getOriginalFilename();
        String nomMinuscule = nom.toLowerCase(Locale.ROOT);
        if (!nomMinuscule.endsWith(".csv") && !nomMinuscule.endsWith(".txt")) {
            throw new RegleMetierException("Le fichier doit être un fichier CSV (.csv).");
        }
        try (InputStream flux = fichier.getInputStream()) {
            return csvImportService.importerClients(nom, flux);
        } catch (IOException e) {
            throw new RegleMetierException("Le fichier est illisible.");
        }
    }

    @GetMapping("/journal")
    public Page<JournalImportDto> journal(
            @PageableDefault(size = 10, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        return csvImportService.historique(pageable);
    }

    @GetMapping("/modele-clients")
    public ResponseEntity<byte[]> modeleClients() {
        byte[] contenu = csvImportService.modeleClients().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("modele-clients.csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenu);
    }
}

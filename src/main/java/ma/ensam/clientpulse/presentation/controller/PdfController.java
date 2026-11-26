package ma.ensam.clientpulse.presentation.controller;

import ma.ensam.clientpulse.dto.DocumentPdfDto;
import ma.ensam.clientpulse.service.interfaces.PdfService;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Devis et factures en PDF.
 *
 * GET /api/devis/{id}/pdf      (ADMIN, MANAGER, COMMERCIAL, COMPTABLE)
 * GET /api/factures/{id}/pdf   (ADMIN, MANAGER, COMPTABLE)
 */
@RestController
@RequestMapping("/api")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/devis/{id}/pdf")
    public ResponseEntity<byte[]> devis(@PathVariable Long id) {
        return reponse(pdfService.genererDevis(id));
    }

    @GetMapping("/factures/{id}/pdf")
    public ResponseEntity<byte[]> facture(@PathVariable Long id) {
        return reponse(pdfService.genererFacture(id));
    }

    private static ResponseEntity<byte[]> reponse(DocumentPdfDto document) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(document.nomFichier()).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(document.contenu().length)
                .body(document.contenu());
    }
}

package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.DocumentPdfDto;

/** Génération des devis et des factures en PDF (exigence B10). */
public interface PdfService {

    DocumentPdfDto genererDevis(Long devisId);

    /** Facture, avoir ou brouillon de facture : le titre du document s'adapte au type et au statut. */
    DocumentPdfDto genererFacture(Long factureId);
}

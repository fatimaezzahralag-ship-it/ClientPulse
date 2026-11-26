import { inject, Injectable } from '@angular/core';

import { FileDownloadService } from './file-download.service';

/**
 * Devis et factures en PDF.
 *
 * À brancher sur les boutons « Imprimer / PDF » des écrans devis et factures :
 *
 *   this.pdf.ouvrirFacture(facture.id).subscribe();      // aperçu dans un nouvel onglet
 *   this.pdf.telechargerFacture(facture.id).subscribe(); // enregistrement du fichier
 */
@Injectable({ providedIn: 'root' })
export class PdfService {

  private readonly fichiers = inject(FileDownloadService);

  ouvrirFacture(id: number) {
    return this.fichiers.ouvrir(`/api/factures/${id}/pdf`);
  }

  telechargerFacture(id: number) {
    return this.fichiers.telecharger(`/api/factures/${id}/pdf`, `facture-${id}.pdf`);
  }

  ouvrirDevis(id: number) {
    return this.fichiers.ouvrir(`/api/devis/${id}/pdf`);
  }

  telechargerDevis(id: number) {
    return this.fichiers.telecharger(`/api/devis/${id}/pdf`, `devis-${id}.pdf`);
  }
}

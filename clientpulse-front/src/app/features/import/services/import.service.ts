import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import { FileDownloadService } from '../../../core/files/file-download.service';
import { PageJournalImport, RapportImport } from '../models/import.model';

@Injectable({ providedIn: 'root' })
export class ImportService {

  private readonly http = inject(HttpClient);
  private readonly fichiers = inject(FileDownloadService);

  /** Envoie le fichier en multipart (champ « fichier »). Le jeton CSRF est ajouté par l'intercepteur. */
  importerClients(fichier: File): Observable<RapportImport> {
    const formulaire = new FormData();
    formulaire.append('fichier', fichier, fichier.name);
    return this.http.post<RapportImport>('/api/import/clients', formulaire);
  }

  journal(page = 0, taille = 10): Observable<PageJournalImport> {
    const params = new HttpParams().set('page', page).set('size', taille);
    return this.http.get<PageJournalImport>('/api/import/journal', { params });
  }

  telechargerModele() {
    return this.fichiers.telecharger('/api/import/modele-clients', 'modele-clients.csv');
  }
}

import { HttpClient, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { Observable, tap } from 'rxjs';

/**
 * Télécharge un fichier renvoyé par l'API (PDF, CSV) en conservant la session et le jeton CSRF
 * gérés par HttpClient : un simple lien <a href> ne conviendrait pas aux routes protégées.
 */
@Injectable({ providedIn: 'root' })
export class FileDownloadService {

  private readonly http = inject(HttpClient);

  /** Récupère le fichier et déclenche l'enregistrement dans le navigateur. */
  telecharger(url: string, nomParDefaut: string): Observable<HttpResponse<Blob>> {
    return this.http.get(url, { responseType: 'blob', observe: 'response' }).pipe(
      tap((reponse) => {
        const nom = this.nomDepuisEntete(reponse.headers.get('Content-Disposition')) ?? nomParDefaut;
        this.enregistrer(reponse.body as Blob, nom);
      })
    );
  }

  /** Ouvre le fichier dans un nouvel onglet (aperçu PDF) au lieu de l'enregistrer. */
  ouvrir(url: string): Observable<HttpResponse<Blob>> {
    return this.http.get(url, { responseType: 'blob', observe: 'response' }).pipe(
      tap((reponse) => {
        const lien = URL.createObjectURL(reponse.body as Blob);
        window.open(lien, '_blank', 'noopener');
        setTimeout(() => URL.revokeObjectURL(lien), 60_000);
      })
    );
  }

  private enregistrer(contenu: Blob, nom: string): void {
    const lien = URL.createObjectURL(contenu);
    const ancre = document.createElement('a');
    ancre.href = lien;
    ancre.download = nom;
    document.body.appendChild(ancre);
    ancre.click();
    ancre.remove();
    URL.revokeObjectURL(lien);
  }

  /** Extrait filename="..." de l'en-tête Content-Disposition. */
  nomDepuisEntete(entete: string | null): string | null {
    if (!entete) {
      return null;
    }
    const etendu = /filename\*=UTF-8''([^;]+)/i.exec(entete);
    if (etendu) {
      return decodeURIComponent(etendu[1]);
    }
    const simple = /filename="?([^";]+)"?/i.exec(entete);
    return simple ? simple[1] : null;
  }
}

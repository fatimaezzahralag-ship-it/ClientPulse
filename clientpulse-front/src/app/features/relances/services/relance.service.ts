import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import {
  PageResponse,
  Relance,
  RelanceFiltres,
  ResultatRelance
} from '../models/relance.model';

@Injectable({ providedIn: 'root' })
export class RelanceService {

  private readonly http = inject(HttpClient);
  private readonly base = '/api/relances';

  rechercher(filtres: RelanceFiltres): Observable<PageResponse<Relance>> {
    let params = new HttpParams()
      .set('page', filtres.page)
      .set('size', filtres.size);
    if (filtres.statut) {
      params = params.set('statut', filtres.statut);
    }
    return this.http.get<PageResponse<Relance>>(this.base, { params });
  }

  trouver(id: number): Observable<Relance> {
    return this.http.get<Relance>(`${this.base}/${id}`);
  }

  demarrer(id: number): Observable<Relance> {
    return this.http.post<Relance>(`${this.base}/${id}/demarrer`, {});
  }

  traiter(
    id: number,
    resultat: ResultatRelance,
    commentaire: string
  ): Observable<Relance> {
    return this.http.post<Relance>(`${this.base}/${id}/traiter`, {
      resultat,
      commentaire: commentaire.trim() || null
    });
  }

  generer(): Observable<{ relancesCreees: number }> {
    return this.http.post<{ relancesCreees: number }>(
      `${this.base}/generer`,
      {}
    );
  }
}

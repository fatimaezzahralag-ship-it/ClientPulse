import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { map, Observable } from 'rxjs';

import {
  Client,
  ClientFiltres,
  ClientRequest,
  Commercial,
  PageResponse,
  ScoreClient
} from '../models/client.model';

@Injectable({ providedIn: 'root' })
export class ClientService {

  private readonly http = inject(HttpClient);
  private readonly base = '/api/clients';

  rechercher(filtres: ClientFiltres): Observable<PageResponse<Client>> {

    let params = new HttpParams()
      .set('page', filtres.page)
      .set('size', filtres.size);

    if (filtres.keyword?.trim()) {
      params = params.set('keyword', filtres.keyword.trim());
    }
    if (filtres.statut) {
      params = params.set('statut', filtres.statut);
    }
    if (filtres.type) {
      params = params.set('type', filtres.type);
    }
    if (filtres.ville?.trim()) {
      params = params.set('ville', filtres.ville.trim());
    }
    if (filtres.commercialId != null) {
      params = params.set('commercialId', filtres.commercialId);
    }

    return this.http.get<PageResponse<Client>>(this.base, { params });
  }

  trouver(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.base}/${id}`);
  }

  creer(requete: ClientRequest): Observable<Client> {
    return this.http.post<Client>(this.base, requete);
  }

  modifier(id: number, requete: ClientRequest): Observable<Client> {
    return this.http.put<Client>(`${this.base}/${id}`, requete);
  }

  commerciaux(): Observable<Commercial[]> {
    return this.http.get<Commercial[]>('/api/commerciaux');
  }

  /** Dernier score connu ; null si le client n'a encore jamais été évalué. */
  score(id: number): Observable<ScoreClient | null> {
    return this.http
      .get<ScoreClient>(`${this.base}/${id}/score`, {
        observe: 'response'
      })
      .pipe(map((reponse) => reponse.body ?? null));
  }

  recalculerScore(id: number): Observable<ScoreClient> {
    return this.http.post<ScoreClient>(`${this.base}/${id}/score`, {});
  }

  historiqueScore(id: number): Observable<ScoreClient[]> {
    return this.http.get<ScoreClient[]>(`${this.base}/${id}/score/historique`);
  }
}

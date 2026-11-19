import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import {
  Interaction,
  InteractionRequest,
  StatutInteraction,
  TypeInteraction
} from '../models/interaction.model';
import { PageResponse } from '../models/client.model';

@Injectable({ providedIn: 'root' })
export class InteractionService {

  private readonly http = inject(HttpClient);

  lister(
    clientId: number,
    page = 0,
    size = 5,
    type?: TypeInteraction,
    statut?: StatutInteraction
  ): Observable<PageResponse<Interaction>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (type) {
      params = params.set('type', type);
    }
    if (statut) {
      params = params.set('statut', statut);
    }

    return this.http.get<PageResponse<Interaction>>(
      `/api/clients/${clientId}/interactions`,
      { params }
    );
  }

  ajouter(
    clientId: number,
    requete: InteractionRequest
  ): Observable<Interaction> {
    return this.http.post<Interaction>(
      `/api/clients/${clientId}/interactions`,
      requete
    );
  }

  changerStatut(
    interactionId: number,
    statut: StatutInteraction
  ): Observable<Interaction> {
    const params = new HttpParams().set('statut', statut);
    return this.http.patch<Interaction>(
      `/api/interactions/${interactionId}/statut`,
      null,
      { params }
    );
  }
}

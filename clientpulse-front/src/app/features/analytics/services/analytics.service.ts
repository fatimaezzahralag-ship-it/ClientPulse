import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import {
  AnalyticsDashboard,
  ClientARelancer,
  NiveauRisque,
  PageResponse,
  ParametreScoring,
  ScoreClient
} from '../models/analytics.model';

/** Lectures analytiques et paramètres du moteur de scoring. */
@Injectable({ providedIn: 'root' })
export class AnalyticsService {

  private readonly http = inject(HttpClient);
  private readonly base = '/api/analytics';

  dashboard(): Observable<AnalyticsDashboard> {
    return this.http.get<AnalyticsDashboard>(`${this.base}/dashboard`);
  }

  scores(
    niveau: NiveauRisque | '',
    page: number,
    size: number
  ): Observable<PageResponse<ScoreClient>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (niveau) {
      params = params.set('niveau', niveau);
    }
    return this.http.get<PageResponse<ScoreClient>>(
      `${this.base}/scores`,
      { params }
    );
  }

  segments(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.base}/segments`);
  }

  niveaux(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.base}/niveaux`);
  }

  clientsARelancer(
    niveau: NiveauRisque | '',
    page: number,
    size: number
  ): Observable<PageResponse<ClientARelancer>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (niveau) {
      params = params.set('niveau', niveau);
    }
    return this.http.get<PageResponse<ClientARelancer>>(
      `${this.base}/clients-a-relancer`,
      { params }
    );
  }

  scoreClient(clientId: number): Observable<ScoreClient> {
    return this.http.get<ScoreClient>(
      `${this.base}/clients/${clientId}/score`
    );
  }

  recalculer(): Observable<void> {
    return this.http.post<void>(`${this.base}/recalculer`, {});
  }

  parametres(): Observable<ParametreScoring[]> {
    return this.http.get<ParametreScoring[]>('/api/parametres');
  }

  modifierParametre(
    id: number,
    valeur: number
  ): Observable<ParametreScoring> {
    return this.http.put<ParametreScoring>(
      `/api/parametres/${id}`,
      { valeur }
    );
  }
}

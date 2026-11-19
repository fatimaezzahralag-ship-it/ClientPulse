import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import { Utilisateur, UtilisateurRequest } from '../models/utilisateur.model';

@Injectable({ providedIn: 'root' })
export class UtilisateurService {

  private readonly http = inject(HttpClient);
  private readonly base = '/api/utilisateurs';

  findAll(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(this.base);
  }

  findById(id: number): Observable<Utilisateur> {
    return this.http.get<Utilisateur>(`${this.base}/${id}`);
  }

  rolesDisponibles(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/roles`);
  }

  creer(requete: UtilisateurRequest): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(this.base, requete);
  }

  modifier(
    id: number,
    requete: UtilisateurRequest
  ): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.base}/${id}`, requete);
  }

  desactiver(id: number): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/deactivate`, {});
  }
}

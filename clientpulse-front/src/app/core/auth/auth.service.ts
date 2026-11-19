import { HttpClient } from '@angular/common/http';
import { computed, Injectable, signal } from '@angular/core';

import { Observable, switchMap, tap } from 'rxjs';

import { Utilisateur } from './auth.model';

const CLE_STOCKAGE = 'clientpulse.utilisateur';

/**
 * Session utilisateur côté navigateur.
 *
 * Le backend utilise une session HTTP + un jeton CSRF :
 *  1. GET /api/auth/csrf dépose le cookie XSRF-TOKEN ;
 *  2. POST /api/auth/login ouvre la session et renvoie l'utilisateur.
 *
 * L'utilisateur est mémorisé dans localStorage uniquement pour restaurer
 * l'affichage après un rechargement. Toute expiration réelle est détectée par
 * le premier appel API qui répond 401 (voir apiInterceptor).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly utilisateurCourant = signal<Utilisateur | null>(
    this.lireStockage()
  );

  readonly utilisateur = this.utilisateurCourant.asReadonly();

  readonly connecte = computed(() => this.utilisateurCourant() !== null);

  readonly nomComplet = computed(() => {
    const utilisateur = this.utilisateurCourant();
    return utilisateur
      ? `${utilisateur.prenom} ${utilisateur.nom}`.trim()
      : '';
  });

  readonly rolePrincipal = computed(
    () => this.utilisateurCourant()?.roles?.[0] ?? ''
  );

  constructor(private readonly http: HttpClient) {}

  /** Dépose le cookie CSRF avant toute requête mutante (login inclus). */
  prechargerJetonCsrf(): Observable<void> {
    return this.http.get<void>('/api/auth/csrf');
  }

  connexion(email: string, motDePasse: string): Observable<Utilisateur> {
    return this.prechargerJetonCsrf().pipe(
      switchMap(() =>
        this.http.post<Utilisateur>('/api/auth/login', {
          email,
          motDePasse
        })
      ),
      tap((utilisateur) => this.enregistrer(utilisateur))
    );
  }

  /** Ferme la session serveur puis efface l'état local, même en cas d'erreur. */
  deconnexion(): void {
    this.http
      .post<void>('/api/auth/logout', {})
      .subscribe({ error: () => this.deconnexionLocale() });
    this.deconnexionLocale();
  }

  deconnexionLocale(): void {
    localStorage.removeItem(CLE_STOCKAGE);
    this.utilisateurCourant.set(null);
  }

  private enregistrer(utilisateur: Utilisateur): void {
    localStorage.setItem(CLE_STOCKAGE, JSON.stringify(utilisateur));
    this.utilisateurCourant.set(utilisateur);
  }

  private lireStockage(): Utilisateur | null {
    const brut = localStorage.getItem(CLE_STOCKAGE);
    if (!brut) {
      return null;
    }
    try {
      return JSON.parse(brut) as Utilisateur;
    } catch {
      localStorage.removeItem(CLE_STOCKAGE);
      return null;
    }
  }
}

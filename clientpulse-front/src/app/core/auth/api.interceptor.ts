import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

/**
 * Ajoute les cookies de session à chaque appel et gère l'expiration.
 *
 * Le jeton CSRF lui-même est ajouté automatiquement par HttpClient : il lit le
 * cookie XSRF-TOKEN et renseigne l'en-tête X-XSRF-TOKEN sur les requêtes
 * mutantes (configuration dans app.config.ts).
 */
export const apiInterceptor: HttpInterceptorFn = (requete, suivant) => {

  const auth = inject(AuthService);
  const router = inject(Router);

  const requeteAvecSession = requete.clone({ withCredentials: true });

  return suivant(requeteAvecSession).pipe(
    catchError((erreur: unknown) => {

      const estAuthentification =
        requete.url.includes('/api/auth/login') ||
        requete.url.includes('/api/auth/logout') ||
        requete.url.includes('/api/auth/csrf');

      if (
        erreur instanceof HttpErrorResponse &&
        erreur.status === 401 &&
        !estAuthentification
      ) {
        auth.deconnexionLocale();
        void router.navigate(['/login'], {
          queryParams: { retour: router.url }
        });
      }

      return throwError(() => erreur);
    })
  );
};

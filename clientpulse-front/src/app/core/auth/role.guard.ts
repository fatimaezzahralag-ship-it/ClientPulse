import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/**
 * Restreint une route aux rôles donnés (contrôle d'affichage uniquement :
 * le backend reste l'autorité en dernier ressort via SecurityConfig).
 */
export const roleGuard = (roles: string[]): CanActivateFn => () => {

  const auth = inject(AuthService);
  const router = inject(Router);

  const utilisateur = auth.utilisateur();
  if (!utilisateur) {
    return router.createUrlTree(['/login'], {
      queryParams: { retour: router.url }
    });
  }

  const autorise = roles.some(
    (role) => utilisateur.roles?.includes(role) ?? false
  );

  return autorise ? true : router.createUrlTree(['/clients']);
};

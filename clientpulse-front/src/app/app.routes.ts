import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

const ROLES_ANALYTICS = ['ADMIN', 'MANAGER', 'ANALYSTE'];
const ROLES_RELANCES = ['ADMIN', 'MANAGER', 'COMMERCIAL'];
const ROLES_FACTURATION = ['ADMIN', 'MANAGER', 'COMMERCIAL', 'COMPTABLE'];

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'clients'
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login/login.component').then(
        (m) => m.LoginComponent
      )
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/shell/shell.component').then(
        (m) => m.ShellComponent
      ),
    children: [
      {
        path: 'clients',
        loadComponent: () =>
          import(
            './features/clients/pages/client-list/client-list.component'
          ).then((m) => m.ClientListComponent)
      },
      {
        path: 'clients/nouveau',
        loadComponent: () =>
          import(
            './features/clients/pages/client-form/client-form.component'
          ).then((m) => m.ClientFormComponent)
      },
      {
        path: 'clients/:id',
        loadComponent: () =>
          import(
            './features/clients/pages/client-detail/client-detail.component'
          ).then((m) => m.ClientDetailComponent)
      },
      {
        path: 'clients/:id/modifier',
        loadComponent: () =>
          import(
            './features/clients/pages/client-form/client-form.component'
          ).then((m) => m.ClientFormComponent)
      },

      // ------------------------------------------------------------ P1 : facturation
      {
        path: 'produits',
        canActivate: [roleGuard(ROLES_FACTURATION)],
        loadComponent: () =>
          import('./features/produits/produits').then((m) => m.Produits)
      },
      {
        path: 'devis',
        canActivate: [roleGuard(ROLES_FACTURATION)],
        loadComponent: () =>
          import('./features/devis/devis-liste').then((m) => m.DevisListe)
      },
      {
        path: 'devis/nouveau',
        canActivate: [roleGuard(ROLES_FACTURATION)],
        loadComponent: () =>
          import('./features/devis/devis-form').then((m) => m.DevisForm)
      },
      {
        path: 'devis/:id',
        canActivate: [roleGuard(ROLES_FACTURATION)],
        loadComponent: () =>
          import('./features/devis/devis-form').then((m) => m.DevisForm)
      },
      {
        path: 'factures',
        canActivate: [roleGuard(ROLES_FACTURATION)],
        loadComponent: () =>
          import('./features/factures/factures-liste').then(
            (m) => m.FacturesListe
          )
      },
      {
        path: 'factures/:id',
        canActivate: [roleGuard(ROLES_FACTURATION)],
        loadComponent: () =>
          import('./features/factures/facture-detail').then(
            (m) => m.FactureDetail
          )
      },
      {
        path: 'balance-agee',
        canActivate: [roleGuard(ROLES_FACTURATION)],
        loadComponent: () =>
          import('./features/balance-agee/balance-agee').then(
            (m) => m.BalanceAgeeEcran
          )
      },

      // ------------------------------------------------------------ J4 : analytics
      {
        path: 'analytics',
        canActivate: [roleGuard(ROLES_ANALYTICS)],
        loadComponent: () =>
          import(
            './features/analytics/pages/dashboard/dashboard.component'
          ).then((m) => m.DashboardComponent)
      },
      {
        path: 'analytics/clients-a-relancer',
        canActivate: [roleGuard(ROLES_ANALYTICS)],
        loadComponent: () =>
          import(
            './features/analytics/pages/at-risk-clients/at-risk-clients.component'
          ).then((m) => m.AtRiskClientsComponent)
      },
      {
        path: 'analytics/clients/:id',
        canActivate: [roleGuard(ROLES_ANALYTICS)],
        loadComponent: () =>
          import(
            './features/analytics/pages/client-risk-detail/client-risk-detail.component'
          ).then((m) => m.ClientRiskDetailComponent)
      },

      // ------------------------------------------------------------ J4 : relances
      {
        path: 'relances',
        canActivate: [roleGuard(ROLES_RELANCES)],
        loadComponent: () =>
          import(
            './features/relances/pages/relance-list/relance-list.component'
          ).then((m) => m.RelanceListComponent)
      },
      {
        path: 'relances/:id',
        canActivate: [roleGuard(ROLES_RELANCES)],
        loadComponent: () =>
          import(
            './features/relances/pages/relance-detail/relance-detail.component'
          ).then((m) => m.RelanceDetailComponent)
      },

      // ------------------------------------------------------------ Personne 3 : tableau de bord, import, paramètres
      {
        path: 'dashboard',
        canActivate: [roleGuard(ROLES_ANALYTICS)],
        loadComponent: () =>
          import(
            './features/dashboard/pages/kpi-dashboard/kpi-dashboard.component'
          ).then((m) => m.KpiDashboardComponent)
      },
      {
        path: 'import',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import(
            './features/import/pages/import-csv/import-csv.component'
          ).then((m) => m.ImportCsvComponent)
      },
      {
        path: 'parametres',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import(
            './features/parametres/pages/parametres/parametres.component'
          ).then((m) => m.ParametresComponent)
      },

      // ------------------------------------------------------------ J4 : utilisateurs
      {
        path: 'utilisateurs',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import(
            './features/utilisateurs/pages/utilisateur-list/utilisateur-list.component'
          ).then((m) => m.UtilisateurListComponent)
      },
      {
        path: 'utilisateurs/nouveau',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import(
            './features/utilisateurs/pages/utilisateur-form/utilisateur-form.component'
          ).then((m) => m.UtilisateurFormComponent)
      },
      {
        path: 'utilisateurs/:id',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import(
            './features/utilisateurs/pages/utilisateur-form/utilisateur-form.component'
          ).then((m) => m.UtilisateurFormComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'clients'
  }
];

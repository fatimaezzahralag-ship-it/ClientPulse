import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';

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
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'clients'
  }
];

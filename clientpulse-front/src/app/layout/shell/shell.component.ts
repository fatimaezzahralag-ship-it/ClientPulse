import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent {

  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly initiales = computed(() => {
    const utilisateur = this.auth.utilisateur();
    if (!utilisateur) {
      return '?';
    }
    const prenom = utilisateur.prenom?.charAt(0) ?? '';
    const nom = utilisateur.nom?.charAt(0) ?? '';
    return `${prenom}${nom}`.toUpperCase();
  });

  protected deconnexion(): void {
    this.auth.deconnexion();
    void this.router.navigate(['/login']);
  }
}

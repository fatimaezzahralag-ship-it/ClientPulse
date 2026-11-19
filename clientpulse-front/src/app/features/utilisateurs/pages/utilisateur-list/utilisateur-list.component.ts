import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UtilisateurService } from '../../services/utilisateur.service';
import {
  LIBELLES_ROLES,
  RoleNom,
  Utilisateur
} from '../../models/utilisateur.model';

@Component({
  selector: 'app-utilisateur-list',
  imports: [RouterLink],
  templateUrl: './utilisateur-list.component.html',
  styleUrl: './utilisateur-list.component.css'
})
export class UtilisateurListComponent implements OnInit {

  private readonly service = inject(UtilisateurService);

  protected readonly utilisateurs = signal<Utilisateur[]>([]);
  protected readonly chargement = signal(false);
  protected readonly erreur = signal('');
  protected readonly message = signal('');
  protected readonly suppressionEnCours = signal<number | null>(null);

  ngOnInit(): void {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.service.findAll().subscribe({
      next: (utilisateurs) => {
        this.utilisateurs.set(utilisateurs);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les utilisateurs.');
        this.chargement.set(false);
      }
    });
  }

  protected desactiver(utilisateur: Utilisateur): void {
    if (!window.confirm(
      `Désactiver le compte de ${utilisateur.prenom} ${utilisateur.nom} ?`
    )) {
      return;
    }

    this.suppressionEnCours.set(utilisateur.id);
    this.erreur.set('');
    this.message.set('');

    this.service.desactiver(utilisateur.id).subscribe({
      next: () => {
        this.message.set('Compte désactivé.');
        this.suppressionEnCours.set(null);
        this.charger();
      },
      error: () => {
        this.erreur.set('Impossible de désactiver ce compte.');
        this.suppressionEnCours.set(null);
      }
    });
  }

  protected libelleRole(role: string): string {
    return LIBELLES_ROLES[role as RoleNom] ?? role;
  }
}

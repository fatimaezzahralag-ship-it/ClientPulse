import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { UtilisateurService } from '../../services/utilisateur.service';
import {
  LIBELLES_ROLES,
  ROLES,
  UtilisateurRequest
} from '../../models/utilisateur.model';

@Component({
  selector: 'app-utilisateur-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './utilisateur-form.component.html',
  styleUrl: './utilisateur-form.component.css'
})
export class UtilisateurFormComponent implements OnInit {

  private readonly service = inject(UtilisateurService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly roles = ROLES;
  protected readonly libellesRoles = LIBELLES_ROLES;

  protected readonly formulaire = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(8)]]
  });

  protected readonly identifiant = signal<number | null>(null);
  protected readonly rolesSelectionnes = signal<Set<string>>(new Set());
  protected readonly chargement = signal(false);
  protected readonly enregistrement = signal(false);
  protected readonly erreur = signal('');

  protected readonly estModification = computed(
    () => this.identifiant() !== null
  );

  ngOnInit(): void {
    const brut = this.route.snapshot.paramMap.get('id');

    if (!brut || brut === 'nouveau') {
      return;
    }

    const identifiant = Number(brut);
    if (Number.isNaN(identifiant)) {
      this.erreur.set('Identifiant utilisateur invalide.');
      return;
    }

    this.identifiant.set(identifiant);

    // En modification, le mot de passe est facultatif : laisser vide conserve
    // le mot de passe actuel côté backend.
    this.formulaire.controls.motDePasse.clearValidators();
    this.formulaire.controls.motDePasse.updateValueAndValidity();

    this.charger(identifiant);
  }

  protected charger(id: number): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.service.findById(id).subscribe({
      next: (utilisateur) => {
        this.formulaire.patchValue({
          nom: utilisateur.nom,
          prenom: utilisateur.prenom,
          email: utilisateur.email,
          motDePasse: ''
        });
        this.rolesSelectionnes.set(new Set(utilisateur.roles));
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Utilisateur introuvable.');
        this.chargement.set(false);
      }
    });
  }

  protected basculerRole(role: string, coche: boolean): void {
    this.rolesSelectionnes.update((selection) => {
      const suivante = new Set(selection);
      if (coche) {
        suivante.add(role);
      } else {
        suivante.delete(role);
      }
      return suivante;
    });
  }

  protected enregistrer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    const roles = [...this.rolesSelectionnes()];
    if (roles.length === 0) {
      this.erreur.set('Sélectionnez au moins un rôle.');
      return;
    }

    const valeurs = this.formulaire.getRawValue();
    const requete: UtilisateurRequest = {
      nom: valeurs.nom.trim(),
      prenom: valeurs.prenom.trim(),
      email: valeurs.email.trim().toLowerCase(),
      motDePasse: valeurs.motDePasse || undefined,
      roles
    };

    this.enregistrement.set(true);
    this.erreur.set('');

    const identifiant = this.identifiant();
    const appel = identifiant !== null
      ? this.service.modifier(identifiant, requete)
      : this.service.creer(requete);

    appel.subscribe({
      next: () => this.router.navigate(['/utilisateurs']),
      error: (erreur: { error?: { detail?: string } }) => {
        this.erreur.set(
          erreur.error?.detail ?? 'Impossible d’enregistrer cet utilisateur.'
        );
        this.enregistrement.set(false);
      }
    });
  }
}

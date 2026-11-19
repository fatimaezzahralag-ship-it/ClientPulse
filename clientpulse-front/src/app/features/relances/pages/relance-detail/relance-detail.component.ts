import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { RelanceService } from '../../services/relance.service';
import {
  LIBELLES_RESULTATS,
  LIBELLES_STATUTS,
  Relance,
  RESULTATS,
  ResultatRelance
} from '../../models/relance.model';

@Component({
  selector: 'app-relance-detail',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './relance-detail.component.html',
  styleUrl: './relance-detail.component.css'
})
export class RelanceDetailComponent implements OnInit {

  private readonly relanceService = inject(RelanceService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  protected readonly resultats = RESULTATS;
  protected readonly libellesResultats = LIBELLES_RESULTATS;
  protected readonly libellesStatuts = LIBELLES_STATUTS;

  protected readonly formulaire = this.fb.nonNullable.group({
    resultat: ['INTERESSE' as ResultatRelance, Validators.required],
    commentaire: ['']
  });

  protected readonly relance = signal<Relance | null>(null);
  protected readonly chargement = signal(false);
  protected readonly actionEnCours = signal(false);
  protected readonly erreur = signal('');
  protected readonly message = signal('');

  ngOnInit(): void {
    const brut = this.route.snapshot.paramMap.get('id');
    const identifiant = Number(brut);

    if (!brut || Number.isNaN(identifiant)) {
      this.erreur.set('Identifiant de relance invalide.');
      return;
    }

    this.charger(identifiant);
  }

  protected charger(id: number): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.relanceService.trouver(id).subscribe({
      next: (relance) => {
        this.relance.set(relance);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Relance introuvable.');
        this.chargement.set(false);
      }
    });
  }

  protected demarrer(): void {
    const relanceCourante = this.relance();
    if (!relanceCourante) {
      return;
    }

    this.actionEnCours.set(true);
    this.erreur.set('');
    this.message.set('');

    this.relanceService.demarrer(relanceCourante.id).subscribe({
      next: (relance) => {
        this.relance.set(relance);
        this.message.set('Relance démarrée.');
        this.actionEnCours.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de démarrer cette relance.');
        this.actionEnCours.set(false);
      }
    });
  }

  protected traiter(): void {
    const relanceCourante = this.relance();
    if (!relanceCourante || this.formulaire.invalid) {
      return;
    }

    const valeurs = this.formulaire.getRawValue();

    this.actionEnCours.set(true);
    this.erreur.set('');
    this.message.set('');

    this.relanceService.traiter(
      relanceCourante.id,
      valeurs.resultat,
      valeurs.commentaire
    ).subscribe({
      next: (relance) => {
        this.relance.set(relance);
        this.message.set('Résultat enregistré. La relance est terminée.');
        this.actionEnCours.set(false);
      },
      error: () => {
        this.erreur.set('Impossible d’enregistrer le résultat.');
        this.actionEnCours.set(false);
      }
    });
  }

  protected classeStatut(statut: Relance['statut']): string {
    switch (statut) {
      case 'A_FAIRE':
        return 'badge badge-prospect';
      case 'EN_COURS':
        return 'badge badge-medium';
      default:
        return 'badge badge-active';
    }
  }
}

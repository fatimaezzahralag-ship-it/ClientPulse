import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { RelanceService } from '../../services/relance.service';
import {
  LIBELLES_RESULTATS,
  LIBELLES_STATUTS,
  Relance,
  STATUTS,
  StatutRelance
} from '../../models/relance.model';

@Component({
  selector: 'app-relance-list',
  imports: [DatePipe, RouterLink],
  templateUrl: './relance-list.component.html',
  styleUrl: './relance-list.component.css'
})
export class RelanceListComponent implements OnInit {

  private readonly relanceService = inject(RelanceService);

  protected readonly statuts = STATUTS;
  protected readonly libellesStatuts = LIBELLES_STATUTS;
  protected readonly libellesResultats = LIBELLES_RESULTATS;

  protected readonly statut = signal<StatutRelance | ''>('');
  protected readonly relances = signal<Relance[]>([]);
  protected readonly page = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly chargement = signal(false);
  protected readonly generationEnCours = signal(false);
  protected readonly erreur = signal('');
  protected readonly message = signal('');

  ngOnInit(): void {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.relanceService.rechercher({
      statut: this.statut(),
      page: this.page(),
      size: 10
    }).subscribe({
      next: (reponse) => {
        this.relances.set(reponse.content);
        this.totalElements.set(reponse.totalElements);
        this.totalPages.set(reponse.totalPages);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les relances.');
        this.chargement.set(false);
      }
    });
  }

  protected changerStatut(valeur: string): void {
    this.statut.set(valeur as StatutRelance | '');
    this.page.set(0);
    this.charger();
  }

  protected pagePrecedente(): void {
    if (this.page() > 0) {
      this.page.update((page) => page - 1);
      this.charger();
    }
  }

  protected pageSuivante(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((page) => page + 1);
      this.charger();
    }
  }

  protected generer(): void {
    this.generationEnCours.set(true);
    this.erreur.set('');
    this.message.set('');

    this.relanceService.generer().subscribe({
      next: (resultat) => {
        this.message.set(
          `${resultat.relancesCreees} relance(s) commerciale(s) créée(s).`
        );
        this.generationEnCours.set(false);
        this.page.set(0);
        this.charger();
      },
      error: () => {
        this.erreur.set('Impossible de générer les relances.');
        this.generationEnCours.set(false);
      }
    });
  }

  protected classeStatut(statut: StatutRelance): string {
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

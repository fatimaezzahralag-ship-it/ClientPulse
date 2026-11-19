import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import {
  Client,
  Commercial,
  NiveauRisque,
  ScoreClient
} from '../../models/client.model';
import { ClientService } from '../../services/client.service';
import { InteractionListComponent } from '../../components/interaction-list/interaction-list.component';

@Component({
  selector: 'app-client-detail',
  imports: [DatePipe, DecimalPipe, RouterLink, InteractionListComponent],
  templateUrl: './client-detail.component.html',
  styleUrl: './client-detail.component.css'
})
export class ClientDetailComponent implements OnInit {

  private readonly clientService = inject(ClientService);
  private readonly route = inject(ActivatedRoute);

  protected readonly client = signal<Client | null>(null);
  protected readonly commerciaux = signal<Commercial[]>([]);
  protected readonly score = signal<ScoreClient | null>(null);
  protected readonly historiqueScore = signal<ScoreClient[]>([]);
  protected readonly chargement = signal(false);
  protected readonly calculEnCours = signal(false);
  protected readonly erreur = signal('');

  protected readonly identifiant = signal<number | null>(null);

  /** Un client INACTIF ne doit plus alimenter de nouvelles interactions. */
  protected readonly autoriserAjout = computed(
    () => this.client()?.statut !== 'INACTIF'
  );

  protected readonly nomCommercial = computed(() => {
    const client = this.client();
    if (!client || client.commercialId == null) {
      return 'Non affecté';
    }
    const commercial = this.commerciaux().find(
      (item) => item.id === client.commercialId
    );
    if (!commercial) {
      return `Commercial #${client.commercialId}`;
    }
    const nom = `${commercial.prenom ?? ''} ${commercial.nom ?? ''}`.trim();
    return nom || commercial.email || `Commercial #${commercial.id}`;
  });

  ngOnInit(): void {
    const parametre = this.route.snapshot.paramMap.get('id');
    const identifiant = Number(parametre);

    if (!parametre || Number.isNaN(identifiant)) {
      this.erreur.set('Identifiant client invalide.');
      return;
    }

    this.identifiant.set(identifiant);
    this.chargerCommerciaux();
    this.chargerClient(identifiant);
    this.chargerScore(identifiant);
    this.chargerHistoriqueScore(identifiant);
  }

  protected recalculer(): void {
    const identifiant = this.identifiant();
    if (!identifiant) {
      return;
    }

    this.calculEnCours.set(true);
    this.erreur.set('');

    this.clientService.recalculerScore(identifiant).subscribe({
      next: (score) => {
        this.score.set(score);
        this.calculEnCours.set(false);
        this.chargerHistoriqueScore(identifiant);
      },
      error: (erreur: unknown) => {
        this.erreur.set(this.messageDErreur(erreur));
        this.calculEnCours.set(false);
      }
    });
  }

  protected classeNiveau(niveau: NiveauRisque): string {
    switch (niveau) {
      case 'FAIBLE':
        return 'badge badge-low';
      case 'MOYEN':
        return 'badge badge-medium';
      case 'ELEVE':
        return 'badge badge-high';
      default:
        return 'badge badge-critical';
    }
  }

  protected libelleSegment(segment: string): string {
    const libelles: Record<string, string> = {
      CHAMPION: 'Champion',
      LOYAL: 'Fidèle',
      NOUVEAU: 'Nouveau',
      A_RISQUE: 'À risque',
      HIBERNATION: 'En hibernation',
      PERDU: 'Perdu'
    };
    return libelles[segment] ?? segment;
  }

  protected libelleStatut(statut: string): string {
    switch (statut) {
      case 'ACTIF':
        return 'Actif';
      case 'PROSPECT':
        return 'Prospect';
      default:
        return 'Inactif';
    }
  }

  private chargerClient(id: number): void {
    this.chargement.set(true);
    this.clientService.trouver(id).subscribe({
      next: (client) => {
        this.client.set(client);
        this.chargement.set(false);
      },
      error: (erreur: unknown) => {
        this.erreur.set(this.messageDErreur(erreur));
        this.chargement.set(false);
      }
    });
  }

  private chargerScore(id: number): void {
    this.clientService.score(id).subscribe({
      next: (score) => this.score.set(score),
      error: () => this.score.set(null)
    });
  }

  private chargerHistoriqueScore(id: number): void {
    this.clientService.historiqueScore(id).subscribe({
      next: (scores) => this.historiqueScore.set(scores.slice(0, 5)),
      error: () => this.historiqueScore.set([])
    });
  }

  private chargerCommerciaux(): void {
    this.clientService.commerciaux().subscribe({
      next: (commerciaux) => this.commerciaux.set(commerciaux),
      error: () => this.commerciaux.set([])
    });
  }

  private messageDErreur(erreur: unknown): string {
    if (erreur instanceof HttpErrorResponse) {
      if (erreur.status === 404) {
        return 'Client introuvable.';
      }
      const detail = erreur.error?.detail;
      if (typeof detail === 'string' && detail.length > 0) {
        return detail;
      }
    }
    return 'Une erreur est survenue lors du chargement de la fiche.';
  }
}

import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ClientService } from '../../../clients/services/client.service';
import { AnalyticsService } from '../../services/analytics.service';
import {
  LIBELLES_NIVEAUX,
  LIBELLES_SEGMENTS,
  NiveauRisque,
  ScoreClient
} from '../../models/analytics.model';

@Component({
  selector: 'app-client-risk-detail',
  imports: [DatePipe, DecimalPipe, RouterLink],
  templateUrl: './client-risk-detail.component.html',
  styleUrl: './client-risk-detail.component.css'
})
export class ClientRiskDetailComponent implements OnInit {

  private readonly analytics = inject(AnalyticsService);
  private readonly clientService = inject(ClientService);
  private readonly route = inject(ActivatedRoute);

  protected readonly libellesNiveaux = LIBELLES_NIVEAUX;
  protected readonly libellesSegments = LIBELLES_SEGMENTS;

  protected readonly clientId = signal<number | null>(null);
  protected readonly raisonSociale = signal('');
  protected readonly codeClient = signal('');
  protected readonly score = signal<ScoreClient | null>(null);
  protected readonly historique = signal<ScoreClient[]>([]);
  protected readonly chargement = signal(false);
  protected readonly erreur = signal('');

  ngOnInit(): void {
    const brut = this.route.snapshot.paramMap.get('id');
    const identifiant = Number(brut);

    if (!brut || Number.isNaN(identifiant)) {
      this.erreur.set('Identifiant client invalide.');
      return;
    }

    this.clientId.set(identifiant);
    this.charger();
  }

  protected charger(): void {
    const identifiant = this.clientId();
    if (identifiant == null) {
      return;
    }

    this.chargement.set(true);
    this.erreur.set('');

    this.clientService.trouver(identifiant).subscribe({
      next: (client) => {
        this.raisonSociale.set(client.raisonSociale);
        this.codeClient.set(client.code);
      },
      error: () => this.erreur.set('Client introuvable.')
    });

    this.analytics.scoreClient(identifiant).subscribe({
      next: (score) => {
        this.score.set(score);
        this.chargement.set(false);
      },
      error: (erreur: { status?: number }) => {
        this.erreur.set(
          erreur.status === 404
            ? 'Aucun score disponible pour ce client. Lancez un recalcul.'
            : 'Impossible de charger le score de ce client.'
        );
        this.chargement.set(false);
      }
    });

    this.clientService.historiqueScore(identifiant).subscribe({
      next: (scores) => this.historique.set(scores.slice(0, 6)),
      error: () => this.historique.set([])
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
}

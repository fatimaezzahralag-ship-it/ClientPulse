import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../../core/auth/auth.service';
import { AnalyticsService } from '../../services/analytics.service';
import {
  ClientARelancer,
  LIBELLES_NIVEAUX,
  LIBELLES_SEGMENTS,
  NIVEAUX,
  NiveauRisque,
  PageResponse
} from '../../models/analytics.model';

@Component({
  selector: 'app-at-risk-clients',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './at-risk-clients.component.html',
  styleUrl: './at-risk-clients.component.css'
})
export class AtRiskClientsComponent implements OnInit {

  private readonly analytics = inject(AnalyticsService);
  private readonly auth = inject(AuthService);

  protected readonly niveaux = NIVEAUX;
  protected readonly libellesNiveaux = LIBELLES_NIVEAUX;
  protected readonly libellesSegments = LIBELLES_SEGMENTS;

  protected readonly niveau = signal<NiveauRisque | ''>('');
  protected readonly clients = signal<ClientARelancer[]>([]);
  protected readonly page = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly chargement = signal(false);
  protected readonly erreur = signal('');

  protected readonly peutRecalculer = (() => {
    const roles = this.auth.utilisateur()?.roles ?? [];
    return roles.includes('ADMIN') || roles.includes('MANAGER');
  })();

  ngOnInit(): void {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.analytics.clientsARelancer(
      this.niveau(),
      this.page(),
      10
    ).subscribe({
      next: (reponse: PageResponse<ClientARelancer>) => {
        this.clients.set(reponse.content);
        this.totalElements.set(reponse.totalElements);
        this.totalPages.set(reponse.totalPages);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les clients à relancer.');
        this.chargement.set(false);
      }
    });
  }

  protected changerNiveau(valeur: string): void {
    this.niveau.set(valeur as NiveauRisque | '');
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

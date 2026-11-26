import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { ChartData, ChartOptions } from 'chart.js';

import { AlertComponent } from '../../../../shared/ui/alert/alert.component';
import { ChartComponent } from '../../../../shared/ui/chart/chart.component';
import { EmptyStateComponent } from '../../../../shared/ui/state/empty-state.component';
import { LoadingStateComponent } from '../../../../shared/ui/state/loading-state.component';
import { PageHeaderComponent } from '../../../../shared/ui/page-header/page-header.component';
import { MadPipe } from '../../../../shared/pipes/mad.pipe';
import { Dashboard, PERIODES_DASHBOARD } from '../../models/dashboard.model';
import { DashboardService } from '../../services/dashboard.service';

const BORDEAUX = '#7B2233';
const BORDEAUX_CLAIR = '#D9A5AF';
const ARDOISE = '#64748B';
const PALETTE = ['#2F6F4F', '#7B2233', '#C2842B', '#3B6EA5', '#8A8F9C', '#A33B52', '#5B8C85'];

/**
 * Tableau de bord financier : trésorerie, rétention, CLV, relances (règle 7.10).
 * Distinct de /analytics, qui détaille le scoring de risque client par client.
 */
@Component({
  selector: 'app-kpi-dashboard',
  imports: [
    RouterLink,
    AlertComponent,
    ChartComponent,
    EmptyStateComponent,
    LoadingStateComponent,
    PageHeaderComponent,
    MadPipe
  ],
  templateUrl: './kpi-dashboard.component.html',
  styleUrl: './kpi-dashboard.component.css'
})
export class KpiDashboardComponent implements OnInit {

  private readonly service = inject(DashboardService);

  protected readonly periodes = PERIODES_DASHBOARD;
  protected readonly periode = signal<number>(12);
  protected readonly donnees = signal<Dashboard | null>(null);
  protected readonly chargement = signal(true);
  protected readonly exportEnCours = signal(false);
  protected readonly erreur = signal('');

  // ------------------------------------------------------------------ graphiques

  protected readonly graphiqueMensuel = computed<ChartData<'bar'>>(() => {
    const serie = this.donnees()?.serieMensuelle ?? [];
    return {
      labels: serie.map((p) => this.libelleMois(p.mois)),
      datasets: [
        { label: 'Facturé TTC', data: serie.map((p) => p.facture), backgroundColor: BORDEAUX },
        { label: 'Encaissé', data: serie.map((p) => p.encaisse), backgroundColor: BORDEAUX_CLAIR }
      ]
    };
  });

  protected readonly graphiqueEncours = computed<ChartData<'doughnut'>>(() => {
    const tranches = this.donnees()?.encoursParTranche ?? [];
    return {
      labels: tranches.map((t) => t.tranche),
      datasets: [{
        data: tranches.map((t) => t.montant),
        backgroundColor: ['#2F6F4F', '#E0B64A', '#C2842B', '#B5532F', BORDEAUX],
        borderWidth: 1
      }]
    };
  });

  protected readonly graphiqueClients = computed<ChartData<'bar'>>(() => {
    const clients = this.donnees()?.meilleursClients ?? [];
    return {
      labels: clients.map((c) => c.raisonSociale),
      datasets: [{ label: 'Chiffre d’affaires HT', data: clients.map((c) => c.chiffreAffairesHt), backgroundColor: BORDEAUX }]
    };
  });

  protected readonly graphiqueSegments = computed<ChartData<'doughnut'>>(() => {
    const repartition = this.donnees()?.repartitionSegments ?? {};
    const cles = Object.keys(repartition);
    return {
      labels: cles.map((c) => this.libelleSegment(c)),
      datasets: [{ data: cles.map((c) => repartition[c]), backgroundColor: PALETTE, borderWidth: 1 }]
    };
  });

  protected readonly totalSegments = computed(() =>
    Object.values<number>(this.donnees()?.repartitionSegments ?? {}).reduce((somme, n) => somme + n, 0)
  );

  protected readonly optionsBarres: ChartOptions<'bar'> = {
    plugins: { legend: { position: 'bottom' } },
    scales: { y: { beginAtZero: true, ticks: { color: ARDOISE } }, x: { ticks: { color: ARDOISE } } }
  };

  protected readonly optionsBarresHorizontales: ChartOptions<'bar'> = {
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: { x: { beginAtZero: true, ticks: { color: ARDOISE } } }
  };

  protected readonly optionsAnneaux: ChartOptions<'doughnut'> = {
    plugins: { legend: { position: 'right' } }
  };

  // ------------------------------------------------------------------ cycle de vie

  ngOnInit(): void {
    this.charger();
  }

  protected changerPeriode(mois: number): void {
    if (mois !== this.periode()) {
      this.periode.set(mois);
      this.charger();
    }
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set('');
    this.service.indicateurs(this.periode()).subscribe({
      next: (resultat) => {
        this.donnees.set(resultat);
        this.chargement.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(this.messageErreur(e));
        this.chargement.set(false);
      }
    });
  }

  protected exporter(): void {
    this.exportEnCours.set(true);
    this.service.exporter(this.periode()).subscribe({
      next: () => this.exportEnCours.set(false),
      error: (e: HttpErrorResponse) => {
        this.erreur.set(this.messageErreur(e));
        this.exportEnCours.set(false);
      }
    });
  }

  // ------------------------------------------------------------------ affichage

  protected pct(valeur: number): string {
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 }).format(valeur)} %`;
  }

  protected nombre(valeur: number, decimales = 1): string {
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: decimales }).format(valeur);
  }

  /** 2026-03 -> « mars 26 ». */
  private libelleMois(mois: string): string {
    const [annee, numero] = mois.split('-').map(Number);
    const nom = new Date(annee, numero - 1, 1).toLocaleDateString('fr-FR', { month: 'short' });
    return `${nom} ${String(annee).slice(2)}`;
  }

  private libelleSegment(cle: string): string {
    const libelles: Record<string, string> = {
      CHAMPION: 'Champions',
      LOYAL: 'Clients loyaux',
      NOUVEAU: 'Nouveaux',
      A_RISQUE: 'À risque',
      HIBERNATION: 'En hibernation',
      PERDU: 'Perdus'
    };
    return libelles[cle] ?? cle;
  }

  private messageErreur(e: HttpErrorResponse): string {
    if (e.status === 403) {
      return 'Votre rôle ne donne pas accès au tableau de bord.';
    }
    return e.error?.detail ?? e.error?.message ?? 'Impossible de charger le tableau de bord.';
  }
}

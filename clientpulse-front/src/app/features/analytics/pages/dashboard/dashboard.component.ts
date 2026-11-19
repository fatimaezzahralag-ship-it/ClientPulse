import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../../core/auth/auth.service';
import { AnalyticsService } from '../../services/analytics.service';
import {
  AnalyticsDashboard,
  LIBELLES_NIVEAUX,
  LIBELLES_SEGMENTS,
  NIVEAUX,
  ParametreScoring,
  SEGMENTS
} from '../../models/analytics.model';

interface Repartition {
  nom: string;
  libelle: string;
  valeur: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  private readonly analytics = inject(AnalyticsService);
  private readonly auth = inject(AuthService);

  protected readonly dashboard = signal<AnalyticsDashboard | null>(null);
  protected readonly niveaux = signal<Repartition[]>([]);
  protected readonly segments = signal<Repartition[]>([]);
  protected readonly parametres = signal<ParametreScoring[]>([]);
  protected readonly chargement = signal(false);
  protected readonly chargementParametres = signal(false);
  protected readonly erreur = signal('');
  protected readonly message = signal('');
  protected readonly recalculEnCours = signal(false);
  protected readonly editions = signal<Record<number, string>>({});
  protected readonly parametreEnCours = signal<number | null>(null);

  protected readonly estAdmin = computed(
    () => this.auth.utilisateur()?.roles?.includes('ADMIN') ?? false
  );

  protected readonly peutRecalculer = computed(() => {
    const roles = this.auth.utilisateur()?.roles ?? [];
    return roles.includes('ADMIN') || roles.includes('MANAGER');
  });

  protected readonly pourcentageARisque = computed(() => {
    const donnees = this.dashboard();
    if (!donnees || donnees.clientsEvalues === 0) {
      return 0;
    }
    return Math.round(
      (donnees.clientsARisque / donnees.clientsEvalues) * 100
    );
  });

  ngOnInit(): void {
    this.charger();
    this.chargerParametres();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.analytics.dashboard().subscribe({
      next: (donnees) => {
        this.dashboard.set(donnees);
        this.niveaux.set(NIVEAUX.map((nom) => ({
          nom,
          libelle: LIBELLES_NIVEAUX[nom],
          valeur: donnees.repartitionNiveaux[nom] ?? 0
        })));
        this.segments.set(SEGMENTS.map((nom) => ({
          nom,
          libelle: LIBELLES_SEGMENTS[nom],
          valeur: donnees.repartitionSegments[nom] ?? 0
        })));
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set(
          'Impossible de charger le tableau de bord. Vérifiez que le backend est démarré.'
        );
        this.chargement.set(false);
      }
    });
  }

  protected recalculer(): void {
    this.recalculEnCours.set(true);
    this.message.set('');
    this.erreur.set('');

    this.analytics.recalculer().subscribe({
      next: () => {
        this.message.set(
          'Recalcul terminé. Les scores, segments et relances ont été rafraîchis.'
        );
        this.recalculEnCours.set(false);
        this.charger();
      },
      error: (erreur: { status?: number }) => {
        this.erreur.set(
          erreur.status === 409
            ? 'Un recalcul est déjà en cours.'
            : 'Le recalcul n’a pas pu être lancé.'
        );
        this.recalculEnCours.set(false);
      }
    });
  }

  protected chargerParametres(): void {
    this.chargementParametres.set(true);
    this.analytics.parametres().subscribe({
      next: (parametres) => {
        this.parametres.set(parametres);
        const editions: Record<number, string> = {};
        for (const parametre of parametres) {
          editions[parametre.id] = String(parametre.valeur);
        }
        this.editions.set(editions);
        this.chargementParametres.set(false);
      },
      error: () => this.chargementParametres.set(false)
    });
  }

  protected majEdition(id: number, valeur: string): void {
    this.editions.update((editions) => ({ ...editions, [id]: valeur }));
  }

  protected enregistrerParametre(parametre: ParametreScoring): void {
    const brut = this.editions()[parametre.id];
    const valeur = Number(brut);

    if (brut == null || brut.trim() === '' || Number.isNaN(valeur) || valeur < 0) {
      this.erreur.set('Saisissez une valeur numérique positive.');
      return;
    }

    this.parametreEnCours.set(parametre.id);
    this.erreur.set('');
    this.message.set('');

    this.analytics.modifierParametre(parametre.id, valeur).subscribe({
      next: (modifie) => {
        this.parametres.update((parametres) =>
          parametres.map((item) => item.id === modifie.id ? modifie : item)
        );
        this.message.set(`Paramètre ${modifie.nom} mis à jour.`);
        this.parametreEnCours.set(null);
      },
      error: (erreur: { error?: { detail?: string } }) => {
        this.erreur.set(
          erreur.error?.detail ?? 'Modification refusée par le moteur de scoring.'
        );
        this.parametreEnCours.set(null);
      }
    });
  }

  protected largeur(valeur: number, total: number): number {
    if (total <= 0) {
      return 0;
    }
    return Math.max(valeur > 0 ? 3 : 0, Math.round((valeur / total) * 100));
  }

  protected totalNiveaux(): number {
    return this.niveaux().reduce((somme, item) => somme + item.valeur, 0);
  }

  protected totalSegments(): number {
    return this.segments().reduce((somme, item) => somme + item.valeur, 0);
  }

  protected classeNiveau(nom: string): string {
    return `barre barre-${nom.toLowerCase()}`;
  }

  protected classeSegment(nom: string): string {
    return `barre barre-segment-${nom.toLowerCase().replace('_', '-')}`;
  }
}

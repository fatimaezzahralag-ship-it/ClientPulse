import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { ParametreScoring } from '../../../analytics/models/analytics.model';
import { AnalyticsService } from '../../../analytics/services/analytics.service';
import { AlertComponent } from '../../../../shared/ui/alert/alert.component';
import { LoadingStateComponent } from '../../../../shared/ui/state/loading-state.component';
import { PageHeaderComponent } from '../../../../shared/ui/page-header/page-header.component';

interface LigneParametre extends ParametreScoring {
  libelle: string;
  aide: string;
  saisie: string;       // valeur en cours de saisie (texte, pour accepter « , » et « . »)
  enregistrement: boolean;
}

interface GroupeParametres {
  titre: string;
  description: string;
  lignes: LigneParametre[];
}

const LIBELLES: Record<string, { libelle: string; aide: string }> = {
  POIDS_INACTIVITE: { libelle: 'Poids : inactivité', aide: 'Part du facteur « jours sans achat » dans le score (0 à 1).' },
  POIDS_BAISSE_ACHATS: { libelle: 'Poids : baisse des achats', aide: 'Variation du montant facturé sur 3 mois (0 à 1).' },
  POIDS_RECLAMATIONS: { libelle: 'Poids : réclamations', aide: 'Réclamations ouvertes ou récentes (0 à 1).' },
  POIDS_RETARD_PAIEMENT: { libelle: 'Poids : retards de paiement', aide: 'Retard moyen et part de factures payées en retard (0 à 1).' },
  SEUIL_RISQUE_MOYEN: { libelle: 'Score à partir duquel le risque est moyen', aide: 'Sur 100.' },
  SEUIL_RISQUE_ELEVE: { libelle: 'Score à partir duquel le risque est élevé', aide: 'Sur 100. Au-delà, le client est suggéré à la relance.' },
  SEUIL_RISQUE_CRITIQUE: { libelle: 'Score à partir duquel le risque est critique', aide: 'Sur 100.' },
  DELAI_RELANCE_N1: { libelle: 'Niveau 1 : rappel préventif', aide: 'Jours AVANT l’échéance.' },
  DELAI_RELANCE_N2: { libelle: 'Niveau 2 : rappel amiable', aide: 'Jours après l’échéance.' },
  DELAI_RELANCE_N3: { libelle: 'Niveau 3 : première relance', aide: 'Jours après l’échéance.' },
  DELAI_RELANCE_N4: { libelle: 'Niveau 4 : deuxième relance', aide: 'Jours après l’échéance.' },
  DELAI_RELANCE_N5: { libelle: 'Niveau 5 : mise en demeure', aide: 'Jours après l’échéance.' }
};

const GROUPES: { prefixe: string; titre: string; description: string }[] = [
  { prefixe: 'DELAI_RELANCE_', titre: 'Délais des relances de paiement', description: 'Pris en compte au prochain passage de la tâche de nuit. Les niveaux 2 à 5 doivent rester dans l’ordre croissant.' },
  { prefixe: 'POIDS_', titre: 'Poids du score de risque', description: 'La somme des poids doit faire 1.' },
  { prefixe: 'SEUIL_RISQUE_', titre: 'Niveaux de risque', description: 'Seuils croissants : moyen, élevé, critique.' },
  { prefixe: 'SEUIL_', titre: 'Seuils des facteurs', description: 'Seuils utilisés pour normaliser chaque facteur du score.' },
  { prefixe: 'RFM_', titre: 'Segmentation RFM', description: 'Seuils de récence, fréquence et montant des segments.' }
];

/** Paramétrage du scoring et des relances, regroupé par thème (ADMIN). */
@Component({
  selector: 'app-parametres',
  imports: [FormsModule, AlertComponent, LoadingStateComponent, PageHeaderComponent],
  templateUrl: './parametres.component.html',
  styleUrl: './parametres.component.css'
})
export class ParametresComponent implements OnInit {

  private readonly service = inject(AnalyticsService);

  protected readonly lignes = signal<LigneParametre[]>([]);
  protected readonly chargement = signal(true);
  protected readonly erreur = signal('');
  protected readonly succes = signal('');

  protected readonly groupes = computed<GroupeParametres[]>(() => {
    const restantes = [...this.lignes()];
    const resultat: GroupeParametres[] = [];
    for (const g of GROUPES) {
      const duGroupe = restantes.filter((l) => l.nom.startsWith(g.prefixe));
      duGroupe.forEach((l) => restantes.splice(restantes.indexOf(l), 1));
      if (duGroupe.length > 0) {
        resultat.push({ titre: g.titre, description: g.description, lignes: duGroupe });
      }
    }
    if (restantes.length > 0) {
      resultat.push({ titre: 'Autres paramètres', description: '', lignes: restantes });
    }
    return resultat;
  });

  /**
   * Somme des poids affichés (valeurs en cours de saisie), pour alerter avant l'enregistrement.
   * Méthode et non computed : la saisie modifie les objets en place, sans changer le signal.
   */
  protected sommePoids(): number {
    return this.lignes()
      .filter((l) => l.nom.startsWith('POIDS_'))
      .reduce((somme, l) => somme + (this.versNombre(l.saisie) ?? 0), 0);
  }

  ngOnInit(): void {
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.service.parametres().subscribe({
      next: (parametres) => {
        this.lignes.set(parametres.map((p) => this.versLigne(p)));
        this.chargement.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(e.status === 403 ? 'Réservé à l’administrateur.' : 'Impossible de charger les paramètres.');
        this.chargement.set(false);
      }
    });
  }

  protected modifie(ligne: LigneParametre): boolean {
    const saisie = this.versNombre(ligne.saisie);
    return saisie !== null && saisie !== ligne.valeur;
  }

  protected saisieInvalide(ligne: LigneParametre): boolean {
    const saisie = this.versNombre(ligne.saisie);
    return saisie === null || saisie < 0;
  }

  protected enregistrer(ligne: LigneParametre): void {
    const valeur = this.versNombre(ligne.saisie);
    if (valeur === null || valeur < 0) {
      return;
    }
    this.erreur.set('');
    this.succes.set('');
    this.mettreAJour(ligne.id, { enregistrement: true });

    this.service.modifierParametre(ligne.id, valeur).subscribe({
      next: (maj) => {
        this.mettreAJour(ligne.id, { valeur: maj.valeur, saisie: String(maj.valeur), enregistrement: false });
        this.succes.set(`« ${ligne.libelle} » enregistré. Pris en compte au prochain calcul.`);
      },
      error: (e: HttpErrorResponse) => {
        this.mettreAJour(ligne.id, { enregistrement: false });
        this.erreur.set(e.error?.detail ?? e.error?.message ?? 'Enregistrement impossible.');
      }
    });
  }

  private mettreAJour(id: number, changements: Partial<LigneParametre>): void {
    this.lignes.update((lignes) => lignes.map((l) => (l.id === id ? { ...l, ...changements } : l)));
  }

  private versLigne(p: ParametreScoring): LigneParametre {
    const connu = LIBELLES[p.nom];
    return {
      ...p,
      libelle: connu?.libelle ?? this.humaniser(p.nom),
      aide: connu?.aide ?? '',
      saisie: String(p.valeur),
      enregistrement: false
    };
  }

  /** Accepte « 0,25 » comme « 0.25 ». Renvoie null si la saisie n'est pas un nombre. */
  private versNombre(saisie: string): number | null {
    const normalise = saisie.trim().replace(',', '.');
    if (normalise === '') {
      return null;
    }
    const nombre = Number(normalise);
    return Number.isFinite(nombre) ? nombre : null;
  }

  private humaniser(nom: string): string {
    const texte = nom.toLowerCase().replace(/_/g, ' ');
    return texte.charAt(0).toUpperCase() + texte.slice(1);
  }
}

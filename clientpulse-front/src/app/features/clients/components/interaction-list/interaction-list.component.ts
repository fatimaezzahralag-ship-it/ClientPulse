import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import {
  Component,
  effect,
  inject,
  input,
  signal,
  untracked
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import {
  Interaction,
  InteractionRequest,
  LIBELLES_STATUT_INTERACTION,
  LIBELLES_TYPE_INTERACTION,
  StatutInteraction,
  TypeInteraction
} from '../../models/interaction.model';
import { PageResponse } from '../../models/client.model';
import { InteractionService } from '../../services/interaction.service';

/**
 * Historique des interactions d'un client.
 *
 * Le composant est réutilisable : il reçoit l'identifiant client en entrée et
 * recharge automatiquement la liste quand celui-ci change. L'ajout est
 * désactivé par la fiche client lorsque le client est INACTIF.
 */
@Component({
  selector: 'app-interaction-list',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './interaction-list.component.html',
  styleUrl: './interaction-list.component.css'
})
export class InteractionListComponent {

  readonly clientId = input.required<number>();
  readonly autoriserAjout = input(true);

  private readonly interactionService = inject(InteractionService);
  private readonly fb = inject(FormBuilder);

  protected readonly types = Object.keys(LIBELLES_TYPE_INTERACTION) as TypeInteraction[];
  protected readonly statuts = Object.keys(LIBELLES_STATUT_INTERACTION) as StatutInteraction[];
  protected readonly libellesType = LIBELLES_TYPE_INTERACTION;
  protected readonly libellesStatut = LIBELLES_STATUT_INTERACTION;

  protected readonly taillePage = 5;

  protected readonly interactions = signal<Interaction[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly page = signal(0);
  protected readonly chargement = signal(false);
  protected readonly erreur = signal('');
  protected readonly succes = signal('');
  protected readonly formulaireOuvert = signal(false);
  protected readonly detailOuvert = signal<number | null>(null);

  protected readonly formulaire = this.fb.nonNullable.group({
    type: ['APPEL' as TypeInteraction, [Validators.required]],
    date: [this.maintenantLocal(), [Validators.required]],
    resume: ['', [Validators.required, Validators.maxLength(2000)]],
    statut: ['OUVERTE' as StatutInteraction, [Validators.required]]
  });

  constructor() {
    effect(() => {
      // On ne suit que l'identifiant client : les changements de page, eux,
      // déclenchent explicitement un rechargement via les boutons.
      this.clientId();
      untracked(() => {
        this.page.set(0);
        this.charger();
      });
    });
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.interactionService
      .lister(this.clientId(), this.page(), this.taillePage)
      .subscribe({
        next: (reponse: PageResponse<Interaction>) => {
          this.interactions.set(reponse.content);
          this.totalElements.set(reponse.totalElements);
          this.totalPages.set(reponse.totalPages);
          this.chargement.set(false);
        },
        error: () => {
          this.erreur.set("Impossible de charger l'historique des interactions.");
          this.chargement.set(false);
        }
      });
  }

  protected afficherFormulaire(): void {
    this.formulaireOuvert.set(true);
    this.succes.set('');
    this.formulaire.patchValue({ date: this.maintenantLocal() });
  }

  protected masquerFormulaire(): void {
    this.formulaireOuvert.set(false);
    this.formulaire.reset({
      type: 'APPEL',
      date: this.maintenantLocal(),
      resume: '',
      statut: 'OUVERTE'
    });
  }

  protected ajouter(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    const valeurs = this.formulaire.getRawValue();
    const requete: InteractionRequest = {
      type: valeurs.type,
      date: this.avecSecondes(valeurs.date),
      resume: valeurs.resume.trim(),
      statut: valeurs.statut
    };

    this.erreur.set('');
    this.interactionService.ajouter(this.clientId(), requete).subscribe({
      next: () => {
        this.succes.set('Interaction ajoutée à l’historique.');
        this.masquerFormulaire();
        this.page.set(0);
        this.charger();
      },
      error: (erreur: unknown) => this.erreur.set(this.messageDErreur(erreur))
    });
  }

  protected changerStatut(
    interaction: Interaction,
    evenement: Event
  ): void {
    const nouveauStatut = (evenement.target as HTMLSelectElement)
      .value as StatutInteraction;

    if (nouveauStatut === interaction.statut) {
      return;
    }

    this.erreur.set('');
    this.interactionService
      .changerStatut(interaction.id, nouveauStatut)
      .subscribe({
        next: (miseAJour) => {
          this.interactions.update((liste) =>
            liste.map((item) =>
              item.id === miseAJour.id ? miseAJour : item
            )
          );
          this.succes.set('Statut de l’interaction mis à jour.');
        },
        error: (erreur: unknown) => this.erreur.set(this.messageDErreur(erreur))
      });
  }

  protected basculerDetail(interaction: Interaction): void {
    this.detailOuvert.update((id) =>
      id === interaction.id ? null : interaction.id
    );
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

  private maintenantLocal(): string {
    const maintenant = new Date();
    const decalage = maintenant.getTimezoneOffset() * 60_000;
    return new Date(maintenant.getTime() - decalage)
      .toISOString()
      .slice(0, 16);
  }

  private avecSecondes(dateLocale: string): string {
    return dateLocale.length === 16 ? `${dateLocale}:00` : dateLocale;
  }

  private messageDErreur(erreur: unknown): string {
    if (erreur instanceof HttpErrorResponse) {
      const detail = erreur.error?.detail;
      if (typeof detail === 'string' && detail.length > 0) {
        return detail;
      }
    }
    return "L'opération a échoué. Veuillez réessayer.";
  }
}

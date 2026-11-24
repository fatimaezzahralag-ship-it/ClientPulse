import { Component, input } from '@angular/core';

/** Pastille colorée pour un statut (devis, facture, relance). La couleur vient du CSS : .badge-ACCEPTE, etc. */
@Component({
  selector: 'app-statut',
  template: `<span class="badge" [class]="'badge badge-' + valeur()">{{ valeur()?.replaceAll('_', ' ') }}</span>`,
})
export class StatutBadge {
  readonly valeur = input<string | null | undefined>('');
}

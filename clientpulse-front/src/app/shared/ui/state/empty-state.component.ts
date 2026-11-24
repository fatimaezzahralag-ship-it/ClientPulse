import { Component, input } from '@angular/core';

/** Carte « rien à afficher » : titre et consigne facultative. */
@Component({
  selector: 'app-empty-state',
  template: `
    <div class="card vide">
      <strong>{{ titre() }}</strong>
      @if (detail()) {
        <span class="muted">{{ detail() }}</span>
      }
    </div>
  `,
  styles: `
    .vide {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 24px;
      text-align: center;
      align-items: center;
    }
  `
})
export class EmptyStateComponent {
  readonly titre = input.required<string>();
  readonly detail = input<string>('');
}

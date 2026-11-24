import { Component, input } from '@angular/core';

/** Carte « chargement en cours » avec le spinner global. */
@Component({
  selector: 'app-loading-state',
  template: `
    <div class="card etat">
      <span class="spinner"></span>
      {{ message() }}
    </div>
  `,
  styles: `
    .etat {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 24px;
      color: var(--cp-slate-500);
    }
  `
})
export class LoadingStateComponent {
  readonly message = input<string>('Chargement…');
}

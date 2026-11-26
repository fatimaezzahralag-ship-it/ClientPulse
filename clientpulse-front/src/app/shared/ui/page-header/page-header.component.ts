import { Component, input } from '@angular/core';

/**
 * En-tête de page : titre, sous-titre et zone d'actions (contenu projeté).
 *
 *   <app-page-header titre="Clients" sousTitre="Portefeuille">
 *     <a class="btn btn-primary">Nouveau</a>
 *   </app-page-header>
 */
@Component({
  selector: 'app-page-header',
  template: `
    <div class="page-head">
      <div>
        <h1>{{ titre() }}</h1>
        @if (sousTitre()) {
          <p class="muted">{{ sousTitre() }}</p>
        }
      </div>
      <div class="head-actions"><ng-content /></div>
    </div>
  `,
  styles: `
    .page-head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 4px;
    }
    h1 { font-size: 24px; }
    p { margin: 6px 0 0; font-size: 13px; }
    .head-actions { display: flex; gap: 8px; flex-wrap: wrap; }
  `
})
export class PageHeaderComponent {
  readonly titre = input.required<string>();
  readonly sousTitre = input<string>('');
}

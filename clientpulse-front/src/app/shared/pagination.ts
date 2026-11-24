import { Component, input, output } from '@angular/core';
import { Page } from '../core/models';

@Component({
  selector: 'app-pagination',
  template: `
    @if (page(); as p) {
      <div class="pagination">
        <button class="secondaire" [disabled]="p.number === 0" (click)="changer.emit(p.number - 1)">Précédent</button>
        <span>Page {{ p.number + 1 }} / {{ p.totalPages || 1 }} ({{ p.totalElements }} éléments)</span>
        <button class="secondaire" [disabled]="p.number + 1 >= p.totalPages" (click)="changer.emit(p.number + 1)">Suivant</button>
      </div>
    }`,
})
export class Pagination {
  readonly page = input<Page<unknown> | null>(null);
  readonly changer = output<number>();
}

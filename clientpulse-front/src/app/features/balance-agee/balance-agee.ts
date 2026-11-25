import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FactureApi } from '../../core/api.services';
import { BalanceAgee } from '../../core/models';

@Component({
  host: { class: 'p1' },
  selector: 'app-balance-agee',
  imports: [FormsModule, CurrencyPipe],
  template: `
    <div class="entete">
      <h2>Balance âgée</h2>
      <label>Arrêtée au <input type="date" [(ngModel)]="date" (ngModelChange)="charger()"></label>
    </div>
    <p class="aide">Montants TTC restant à payer, classés selon le retard par rapport à l'échéance.</p>
    <table>
      <thead><tr><th>Client</th><th class="droite">Non échu</th><th class="droite">1-30 j</th><th class="droite">31-60 j</th><th class="droite">61-90 j</th><th class="droite">+ de 90 j</th><th class="droite">Total</th></tr></thead>
      <tbody>
        @for (b of lignes(); track b.clientId) {
          <tr>
            <td>#{{ b.clientId }}</td>
            <td class="droite">{{ b.nonEchu | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="droite">{{ b.retard1a30 | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="droite">{{ b.retard31a60 | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="droite" [class.alerte]="b.retard61a90 > 0">{{ b.retard61a90 | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="droite" [class.alerte]="b.retardPlusDe90 > 0">{{ b.retardPlusDe90 | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="droite"><strong>{{ total(b) | currency:'MAD':'symbol':'1.2-2' }}</strong></td>
          </tr>
        } @empty { <tr><td colspan="7" class="vide">Aucune créance ouverte.</td></tr> }
      </tbody>
      @if (lignes().length) {
        <tfoot><tr>
          <td>Total</td>
          <td class="droite">{{ somme().nonEchu | currency:'MAD':'symbol':'1.2-2' }}</td>
          <td class="droite">{{ somme().retard1a30 | currency:'MAD':'symbol':'1.2-2' }}</td>
          <td class="droite">{{ somme().retard31a60 | currency:'MAD':'symbol':'1.2-2' }}</td>
          <td class="droite">{{ somme().retard61a90 | currency:'MAD':'symbol':'1.2-2' }}</td>
          <td class="droite">{{ somme().retardPlusDe90 | currency:'MAD':'symbol':'1.2-2' }}</td>
          <td class="droite"><strong>{{ total(somme()) | currency:'MAD':'symbol':'1.2-2' }}</strong></td>
        </tr></tfoot>
      }
    </table>`,
})
export class BalanceAgeeEcran implements OnInit {
  private readonly api = inject(FactureApi);
  protected readonly lignes = signal<BalanceAgee[]>([]);
  protected date = new Date().toISOString().slice(0, 10);

  protected readonly somme = computed(() => this.lignes().reduce((s, b) => ({
    clientId: 0,
    nonEchu: s.nonEchu + b.nonEchu,
    retard1a30: s.retard1a30 + b.retard1a30,
    retard31a60: s.retard31a60 + b.retard31a60,
    retard61a90: s.retard61a90 + b.retard61a90,
    retardPlusDe90: s.retardPlusDe90 + b.retardPlusDe90,
  }), { clientId: 0, nonEchu: 0, retard1a30: 0, retard31a60: 0, retard61a90: 0, retardPlusDe90: 0 }));

  ngOnInit(): void { this.charger(); }
  protected charger(): void { this.api.balanceAgee(this.date).subscribe(l => this.lignes.set(l)); }
  protected total(b: BalanceAgee): number {
    return b.nonEchu + b.retard1a30 + b.retard31a60 + b.retard61a90 + b.retardPlusDe90;
  }
}

import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FactureApi } from '../../core/api.services';
import { Facture, Page, StatutFacture } from '../../core/models';
import { Pagination } from '../../shared/pagination';
import { StatutBadge } from '../../shared/statut-badge';

const STATUTS: StatutFacture[] = ['BROUILLON', 'EMISE', 'PARTIELLEMENT_PAYEE', 'PAYEE', 'EN_RETARD', 'ANNULEE'];

@Component({
  host: { class: 'p1' },
  selector: 'app-factures-liste',
  imports: [RouterLink, FormsModule, CurrencyPipe, DatePipe, Pagination, StatutBadge],
  template: `
    <div class="entete">
      <h2>Factures</h2>
      <label>Statut
        <select [(ngModel)]="statut" (ngModelChange)="charger(0)">
          <option value="">Tous</option>
          @for (s of statuts; track s) { <option [value]="s">{{ s.replaceAll('_', ' ') }}</option> }
        </select>
      </label>
    </div>
    <table>
      <thead><tr><th>Numéro</th><th>Type</th><th>Émission</th><th>Échéance</th><th>Client</th><th>Statut</th><th class="droite">Total TTC</th><th class="droite">Reste à payer</th></tr></thead>
      <tbody>
        @for (f of page()?.content; track f.id) {
          <tr>
            <td><a [routerLink]="['/factures', f.id]">{{ f.numero ?? 'Brouillon #' + f.id }}</a></td>
            <td>{{ f.type }}</td>
            <td>{{ f.dateEmission | date:'dd/MM/yyyy' }}</td>
            <td>{{ f.dateEcheance | date:'dd/MM/yyyy' }}</td>
            <td>#{{ f.clientId }}</td>
            <td><app-statut [valeur]="f.statut" /></td>
            <td class="droite">{{ f.totalTTC | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="droite">{{ f.resteAPayer | currency:'MAD':'symbol':'1.2-2' }}</td>
          </tr>
        } @empty { <tr><td colspan="8" class="vide">Aucune facture.</td></tr> }
      </tbody>
    </table>
    <app-pagination [page]="page()" (changer)="charger($event)" />`,
})
export class FacturesListe implements OnInit {
  private readonly api = inject(FactureApi);
  protected readonly page = signal<Page<Facture> | null>(null);
  protected readonly statuts = STATUTS;
  protected statut: StatutFacture | '' = '';

  ngOnInit(): void { this.charger(0); }
  protected charger(n: number): void { this.api.lister(this.statut, n).subscribe(p => this.page.set(p)); }
}

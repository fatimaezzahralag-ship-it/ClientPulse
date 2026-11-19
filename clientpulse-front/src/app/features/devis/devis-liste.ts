import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DevisApi, FactureApi } from '../../core/api.services';
import { Devis, Page } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { Pagination } from '../../shared/pagination';
import { StatutBadge } from '../../shared/statut-badge';

@Component({
  selector: 'app-devis-liste',
  imports: [RouterLink, CurrencyPipe, DatePipe, Pagination, StatutBadge],
  template: `
    <div class="entete">
      <h2>Devis</h2>
      <a class="bouton" routerLink="/devis/nouveau">Nouveau devis</a>
    </div>
    <table>
      <thead><tr><th>Numéro</th><th>Date</th><th>Client</th><th>Statut</th><th class="droite">Total TTC</th><th></th></tr></thead>
      <tbody>
        @for (d of page()?.content; track d.id) {
          <tr>
            <td><a [routerLink]="['/devis', d.id]">{{ d.numero }}</a></td>
            <td>{{ d.dateEmission | date:'dd/MM/yyyy' }}</td>
            <td>#{{ d.clientId }}</td>
            <td><app-statut [valeur]="d.statut" /></td>
            <td class="droite">{{ d.totalTTC | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="actions">
              @if (d.statut === 'BROUILLON') { <button (click)="agir(d, 'envoyer')">Envoyer</button> }
              @if (d.statut === 'ENVOYE') {
                <button (click)="agir(d, 'accepter')">Accepter</button>
                <button class="secondaire" (click)="agir(d, 'refuser')">Refuser</button>
              }
              @if (d.statut === 'ACCEPTE') { <button (click)="facturer(d)">Créer la facture</button> }
            </td>
          </tr>
        } @empty { <tr><td colspan="6" class="vide">Aucun devis.</td></tr> }
      </tbody>
    </table>
    <app-pagination [page]="page()" (changer)="charger($event)" />`,
})
export class DevisListe implements OnInit {
  private readonly api = inject(DevisApi);
  private readonly factures = inject(FactureApi);
  private readonly notif = inject(NotificationService);
  private readonly router = inject(Router);
  protected readonly page = signal<Page<Devis> | null>(null);

  ngOnInit(): void { this.charger(0); }

  protected charger(n: number): void { this.api.lister(n).subscribe(p => this.page.set(p)); }

  protected agir(d: Devis, action: 'envoyer' | 'accepter' | 'refuser'): void {
    this.api[action](d.id!).subscribe(() => {
      this.notif.succes(`Devis ${d.numero} mis à jour.`);
      this.charger(this.page()?.number ?? 0);
    });
  }

  protected facturer(d: Devis): void {
    this.factures.creerDepuisDevis(d.id!).subscribe(f => {
      this.notif.succes('Facture brouillon créée.');
      this.router.navigate(['/factures', f.id]);
    });
  }
}

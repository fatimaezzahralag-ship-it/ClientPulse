import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FactureApi } from '../../core/api.services';
import { Facture, MODES_PAIEMENT, ModePaiement } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { StatutBadge } from '../../shared/statut-badge';

@Component({
  host: { class: 'p1' },
  selector: 'app-facture-detail',
  imports: [RouterLink, FormsModule, CurrencyPipe, DatePipe, StatutBadge],
  template: `
    <div class="entete">
      <h2>{{ f()?.numero ?? 'Facture brouillon' }}</h2>
      <a class="bouton secondaire" routerLink="/factures">Retour</a>
    </div>

    @if (f(); as fa) {
      <div class="carte">
        <p>Statut : <app-statut [valeur]="fa.statut" /> | Type : {{ fa.type }} | Client #{{ fa.clientId }}
          @if (fa.devisId) { | Devis #{{ fa.devisId }} }</p>
        <p>Émission : {{ fa.dateEmission | date:'dd/MM/yyyy' }} | Échéance : {{ fa.dateEcheance | date:'dd/MM/yyyy' }}</p>

        <table>
          <thead><tr><th>Désignation</th><th class="droite">Qté</th><th class="droite">Prix HT</th><th class="droite">Remise %</th><th class="droite">TVA %</th><th class="droite">Montant HT</th></tr></thead>
          <tbody>
            @for (l of fa.lignes; track l.id) {
              <tr>
                <td>{{ l.designation }}</td><td class="droite">{{ l.quantite }}</td>
                <td class="droite">{{ l.prixUnitaireHT | currency:'MAD':'symbol':'1.2-2' }}</td>
                <td class="droite">{{ l.remise }}</td><td class="droite">{{ l.tauxTVA }}</td>
                <td class="droite">{{ l.montantHT | currency:'MAD':'symbol':'1.2-2' }}</td>
              </tr>
            }
          </tbody>
        </table>
        <p class="totaux">HT {{ fa.totalHT | currency:'MAD':'symbol':'1.2-2' }} | TVA {{ fa.totalTVA | currency:'MAD':'symbol':'1.2-2' }}
          | <strong>TTC {{ fa.totalTTC | currency:'MAD':'symbol':'1.2-2' }}</strong>
          | Payé {{ fa.totalPaye | currency:'MAD':'symbol':'1.2-2' }}
          | <strong>Reste {{ fa.resteAPayer | currency:'MAD':'symbol':'1.2-2' }}</strong></p>

        <div class="actions">
          @if (fa.statut === 'BROUILLON') {
            <label>Échéance (facultative) <input type="date" [(ngModel)]="echeance"></label>
            <button (click)="emettre()">Émettre la facture</button>
          }
          @if (fa.statut !== 'ANNULEE' && fa.statut !== 'PAYEE') {
            <button class="danger" (click)="annuler()">Annuler</button>
          }
        </div>
      </div>

      <h3>Paiements</h3>
      <table>
        <thead><tr><th>Date</th><th>Mode</th><th>Référence</th><th class="droite">Montant</th></tr></thead>
        <tbody>
          @for (p of fa.paiements; track p.id) {
            <tr><td>{{ p.datePaiement | date:'dd/MM/yyyy' }}</td><td>{{ p.mode }}</td><td>{{ p.reference }}</td>
              <td class="droite">{{ p.montant | currency:'MAD':'symbol':'1.2-2' }}</td></tr>
          } @empty { <tr><td colspan="4" class="vide">Aucun paiement.</td></tr> }
        </tbody>
      </table>

      @if (peutPayer(fa)) {
        <form class="carte formulaire" (ngSubmit)="payer(fa)">
          <h3>Enregistrer un paiement</h3>
          <label>Date <input name="date" type="date" [(ngModel)]="date" required></label>
          <label>Montant <input name="montant" type="number" step="0.01" min="0.01" [max]="fa.resteAPayer" [(ngModel)]="montant" required></label>
          <label>Mode
            <select name="mode" [(ngModel)]="mode">
              @for (m of modes; track m) { <option [value]="m">{{ m }}</option> }
            </select>
          </label>
          <label>Référence <input name="ref" [(ngModel)]="reference"></label>
          <button type="submit">Enregistrer le paiement</button>
        </form>
      }
    }`,
})
export class FactureDetail implements OnInit {
  readonly id = input.required<string>();
  private readonly api = inject(FactureApi);
  private readonly notif = inject(NotificationService);

  protected readonly f = signal<Facture | null>(null);
  protected readonly modes = MODES_PAIEMENT;
  protected echeance = '';
  protected date = new Date().toISOString().slice(0, 10);
  protected montant: number | null = null;
  protected mode: ModePaiement = 'VIREMENT';
  protected reference = '';

  ngOnInit(): void { this.charger(); }

  private charger(): void { this.api.trouver(+this.id()).subscribe(f => this.f.set(f)); }

  protected peutPayer(f: Facture): boolean {
    return (f.statut === 'EMISE' || f.statut === 'PARTIELLEMENT_PAYEE' || f.statut === 'EN_RETARD') && f.resteAPayer > 0;
  }

  protected emettre(): void {
    this.api.emettre(+this.id(), undefined, this.echeance || undefined).subscribe(f => {
      this.f.set(f);
      this.notif.succes(`Facture ${f.numero} émise.`);
    });
  }

  protected annuler(): void {
    if (!confirm('Annuler cette facture ?')) return;
    this.api.annuler(+this.id()).subscribe(f => { this.f.set(f); this.notif.succes('Facture annulée.'); });
  }

  protected payer(f: Facture): void {
    if (!this.montant) return;
    this.api.payer(f.id, { datePaiement: this.date, montant: this.montant, mode: this.mode, reference: this.reference || null })
      .subscribe(maj => {
        this.f.set(maj);
        this.montant = null;
        this.reference = '';
        this.notif.succes('Paiement enregistré.');
      });
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProduitApi, TauxTvaApi } from '../../core/api.services';
import { Page, Produit, TauxTva } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { Pagination } from '../../shared/pagination';

const VIDE: Produit = { reference: '', designation: '', prixUnitaireHT: 0, marge: null, actif: true, tauxTvaId: 0 };

@Component({
  host: { class: 'p1' },
  selector: 'app-produits',
  imports: [FormsModule, CurrencyPipe, DecimalPipe, Pagination],
  template: `
    <div class="entete">
      <h2>Catalogue produits</h2>
      <div class="actions">
        <input placeholder="Rechercher (référence ou désignation)" [(ngModel)]="recherche" (keyup.enter)="charger(0)">
        <button (click)="charger(0)">Rechercher</button>
        <button (click)="nouveau()">Nouveau produit</button>
      </div>
    </div>

    @if (edition(); as p) {
      <form class="carte formulaire" (ngSubmit)="enregistrer()">
        <h3>{{ p.id ? 'Modifier le produit' : 'Nouveau produit' }}</h3>
        <label>Référence <input name="ref" [(ngModel)]="p.reference" required></label>
        <label>Désignation <input name="des" [(ngModel)]="p.designation" required></label>
        <label>Prix unitaire HT <input name="prix" type="number" step="0.01" min="0" [(ngModel)]="p.prixUnitaireHT" required></label>
        <label>Marge (%) <input name="marge" type="number" step="0.01" min="0" [(ngModel)]="p.marge"></label>
        <label>TVA
          <select name="tva" [(ngModel)]="p.tauxTvaId" required>
            <option [ngValue]="0" disabled>Choisir...</option>
            @for (t of taux(); track t.id) { <option [ngValue]="t.id">{{ t.libelle }} ({{ t.taux }} %)</option> }
          </select>
        </label>
        <label class="ligne"><input name="actif" type="checkbox" [(ngModel)]="p.actif"> Actif</label>
        <div class="actions">
          <button type="submit">Enregistrer</button>
          <button type="button" class="secondaire" (click)="edition.set(null)">Annuler</button>
        </div>
      </form>
    }

    <table>
      <thead><tr><th>Référence</th><th>Désignation</th><th class="droite">Prix HT</th><th class="droite">TVA</th><th>Actif</th><th></th></tr></thead>
      <tbody>
        @for (p of page()?.content; track p.id) {
          <tr [class.inactif]="!p.actif">
            <td>{{ p.reference }}</td>
            <td>{{ p.designation }}</td>
            <td class="droite">{{ p.prixUnitaireHT | currency:'MAD':'symbol':'1.2-2' }}</td>
            <td class="droite">{{ p.tauxTva | number:'1.0-2' }} %</td>
            <td>{{ p.actif ? 'Oui' : 'Non' }}</td>
            <td class="actions">
              <button class="secondaire" (click)="modifier(p)">Modifier</button>
              @if (p.actif) { <button class="danger" (click)="desactiver(p)">Désactiver</button> }
            </td>
          </tr>
        } @empty { <tr><td colspan="6" class="vide">Aucun produit.</td></tr> }
      </tbody>
    </table>
    <app-pagination [page]="page()" (changer)="charger($event)" />`,
})
export class Produits implements OnInit {
  private readonly api = inject(ProduitApi);
  private readonly tvaApi = inject(TauxTvaApi);
  private readonly notif = inject(NotificationService);

  protected readonly page = signal<Page<Produit> | null>(null);
  protected readonly taux = signal<TauxTva[]>([]);
  protected readonly edition = signal<Produit | null>(null);
  protected recherche = '';

  ngOnInit(): void {
    this.tvaApi.lister().subscribe(t => this.taux.set(t));
    this.charger(0);
  }

  protected charger(numero: number): void {
    this.api.lister(this.recherche, numero).subscribe(p => this.page.set(p));
  }

  protected nouveau(): void {
    const defaut = this.taux()[0]?.id ?? 0;
    this.edition.set({ ...VIDE, tauxTvaId: defaut });
  }

  protected modifier(p: Produit): void { this.edition.set({ ...p }); }

  protected enregistrer(): void {
    const p = this.edition();
    if (!p) return;
    const appel = p.id ? this.api.modifier(p.id, p) : this.api.creer(p);
    appel.subscribe(() => {
      this.notif.succes('Produit enregistré.');
      this.edition.set(null);
      this.charger(this.page()?.number ?? 0);
    });
  }

  protected desactiver(p: Produit): void {
    if (!p.id || !confirm(`Désactiver ${p.reference} ?`)) return;
    this.api.desactiver(p.id).subscribe(() => {
      this.notif.succes('Produit désactivé.');
      this.charger(this.page()?.number ?? 0);
    });
  }
}

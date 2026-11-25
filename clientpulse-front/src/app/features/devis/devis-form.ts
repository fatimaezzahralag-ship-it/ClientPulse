import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClientApi, DevisApi, ProduitApi } from '../../core/api.services';
import { ClientResume, Devis, Ligne, Produit } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

const LIGNE_VIDE = (): Ligne => ({ produitId: null, designation: '', quantite: 1, prixUnitaireHT: 0, remise: 0, tauxTVA: 20 });

@Component({
  host: { class: 'p1' },
  selector: 'app-devis-form',
  imports: [FormsModule, RouterLink, CurrencyPipe],
  template: `
    <div class="entete">
      <h2>{{ devis().id ? 'Devis ' + devis().numero : 'Nouveau devis' }}</h2>
      <a class="bouton secondaire" routerLink="/devis">Retour</a>
    </div>

    @if (!modifiable()) {
      <p class="info">Ce devis est {{ devis().statut }} : il n'est plus modifiable.</p>
    }

    <form class="carte formulaire" (ngSubmit)="enregistrer()">
      <label>Client
        @if (clients().length) {
          <select name="client" [(ngModel)]="devis().clientId" [disabled]="!modifiable()" required>
            <option [ngValue]="0" disabled>Choisir...</option>
            @for (c of clients(); track c.id) { <option [ngValue]="c.id">{{ c.raisonSociale ?? c.code ?? c.id }}</option> }
          </select>
        } @else {
          <input name="client" type="number" placeholder="Identifiant du client" [(ngModel)]="devis().clientId" [disabled]="!modifiable()" required>
        }
      </label>
      <label>Date d'émission <input name="de" type="date" [(ngModel)]="devis().dateEmission" [disabled]="!modifiable()"></label>
      <label>Valable jusqu'au <input name="dv" type="date" [(ngModel)]="devis().dateValidite" [disabled]="!modifiable()"></label>

      <h3>Lignes</h3>
      <table class="lignes">
        <thead><tr><th>Produit</th><th>Désignation</th><th>Qté</th><th>Prix HT</th><th>Remise %</th><th>TVA %</th><th class="droite">Total HT</th><th></th></tr></thead>
        <tbody>
          @for (l of devis().lignes; track $index) {
            <tr>
              <td>
                <select [name]="'p' + $index" [ngModel]="l.produitId" (ngModelChange)="choisirProduit(l, $event)" [disabled]="!modifiable()">
                  <option [ngValue]="null">Libre</option>
                  @for (p of produits(); track p.id) { <option [ngValue]="p.id">{{ p.reference }}</option> }
                </select>
              </td>
              <td><input [name]="'d' + $index" [(ngModel)]="l.designation" [disabled]="!modifiable()"></td>
              <td><input [name]="'q' + $index" type="number" min="0.01" step="0.01" [(ngModel)]="l.quantite" [disabled]="!modifiable()"></td>
              <td><input [name]="'u' + $index" type="number" min="0" step="0.01" [(ngModel)]="l.prixUnitaireHT" [disabled]="!modifiable()"></td>
              <td><input [name]="'r' + $index" type="number" min="0" max="100" step="0.01" [(ngModel)]="l.remise" [disabled]="!modifiable()"></td>
              <td><input [name]="'t' + $index" type="number" min="0" max="100" step="0.01" [(ngModel)]="l.tauxTVA" [disabled]="!modifiable()"></td>
              <td class="droite">{{ totalLigne(l) | currency:'MAD':'symbol':'1.2-2' }}</td>
              <td>@if (modifiable()) { <button type="button" class="danger" (click)="retirer($index)">x</button> }</td>
            </tr>
          }
        </tbody>
      </table>
      @if (modifiable()) { <button type="button" class="secondaire" (click)="ajouter()">Ajouter une ligne</button> }

      <p class="totaux">
        Total HT estimé : {{ estimeHT() | currency:'MAD':'symbol':'1.2-2' }}
        @if (devis().totalTTC !== undefined) { | Total TTC (serveur) : {{ devis().totalTTC | currency:'MAD':'symbol':'1.2-2' }} }
      </p>
      <p class="aide">Les totaux officiels sont calculés par le serveur à l'enregistrement.</p>

      @if (modifiable()) { <button type="submit">Enregistrer</button> }
    </form>`,
})
export class DevisForm implements OnInit {
  /** Paramètre de route :id (withComponentInputBinding). Absent pour /devis/nouveau. */
  readonly id = input<string>();

  private readonly api = inject(DevisApi);
  private readonly produitApi = inject(ProduitApi);
  private readonly clientApi = inject(ClientApi);
  private readonly router = inject(Router);
  private readonly notif = inject(NotificationService);

  protected readonly devis = signal<Devis>({ clientId: 0, dateEmission: null, dateValidite: null, lignes: [LIGNE_VIDE()] });
  protected readonly produits = signal<Produit[]>([]);
  protected readonly clients = signal<ClientResume[]>([]);
  protected readonly modifiable = computed(() => !this.devis().statut || this.devis().statut === 'BROUILLON');

  ngOnInit(): void {
    this.produitApi.lister('', 0, 200).subscribe(p => this.produits.set(p.content.filter(x => x.actif)));
    // Si l'endpoint clients de P2 n'existe pas encore, l'erreur est ignorée et on saisit l'identifiant.
    this.clientApi.lister().subscribe({ next: c => this.clients.set(c), error: () => this.clients.set([]) });
    const id = this.id();
    if (id) this.api.trouver(+id).subscribe(d => this.devis.set(d));
  }

  protected choisirProduit(ligne: Ligne, produitId: number | null): void {
    ligne.produitId = produitId;
    const p = this.produits().find(x => x.id === produitId);
    if (p) {
      ligne.designation = p.designation;
      ligne.prixUnitaireHT = p.prixUnitaireHT;
      ligne.tauxTVA = p.tauxTva ?? ligne.tauxTVA;
    }
  }

  protected ajouter(): void { this.devis.update(d => ({ ...d, lignes: [...d.lignes, LIGNE_VIDE()] })); }
  protected retirer(i: number): void { this.devis.update(d => ({ ...d, lignes: d.lignes.filter((_, k) => k !== i) })); }

  protected totalLigne(l: Ligne): number {
    return (l.quantite || 0) * (l.prixUnitaireHT || 0) * (1 - (l.remise || 0) / 100);
  }
  protected estimeHT(): number { return this.devis().lignes.reduce((s, l) => s + this.totalLigne(l), 0); }

  protected enregistrer(): void {
    const d = this.devis();
    if (!d.clientId) { this.notif.erreur('Choisissez un client.'); return; }
    // dateEmission / dateValidite vides doivent partir en null, pas en chaîne vide.
    const corps: Devis = { ...d, dateEmission: d.dateEmission || null, dateValidite: d.dateValidite || null };
    const appel = d.id ? this.api.modifier(d.id, corps) : this.api.creer(corps);
    appel.subscribe(r => {
      this.notif.succes(`Devis ${r.numero} enregistré.`);
      this.router.navigate(['/devis']);
    });
  }
}

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { API } from './config';
import {
  BalanceAgee, ClientResume, Devis, Facture, NatureRelance, Page, Paiement, Produit,
  Relance, ResultatTraitement, StatutFacture, StatutRelance, TauxTva
} from './models';

/** Construit des HttpParams en ignorant les valeurs vides. */
function params(valeurs: Record<string, string | number | boolean | undefined | null>): HttpParams {
  let p = new HttpParams();
  for (const [cle, v] of Object.entries(valeurs)) {
    if (v !== undefined && v !== null && v !== '') p = p.set(cle, String(v));
  }
  return p;
}

@Injectable({ providedIn: 'root' })
export class TauxTvaApi {
  private readonly http = inject(HttpClient);
  lister(actifsSeulement = true): Observable<TauxTva[]> {
    return this.http.get<TauxTva[]>(`${API}/taux-tva`, { params: params({ actifsSeulement }) });
  }
}

@Injectable({ providedIn: 'root' })
export class ProduitApi {
  private readonly http = inject(HttpClient);
  lister(q: string, page: number, size = 10): Observable<Page<Produit>> {
    return this.http.get<Page<Produit>>(`${API}/produits`, { params: params({ q, page, size, sort: 'reference' }) });
  }
  creer(p: Produit): Observable<Produit> { return this.http.post<Produit>(`${API}/produits`, p); }
  modifier(id: number, p: Produit): Observable<Produit> { return this.http.put<Produit>(`${API}/produits/${id}`, p); }
  desactiver(id: number): Observable<Produit> { return this.http.delete<Produit>(`${API}/produits/${id}`); }
}

@Injectable({ providedIn: 'root' })
export class DevisApi {
  private readonly http = inject(HttpClient);
  lister(page: number, size = 10): Observable<Page<Devis>> {
    return this.http.get<Page<Devis>>(`${API}/devis`, { params: params({ page, size, sort: 'id,desc' }) });
  }
  trouver(id: number): Observable<Devis> { return this.http.get<Devis>(`${API}/devis/${id}`); }
  creer(d: Devis): Observable<Devis> { return this.http.post<Devis>(`${API}/devis`, d); }
  modifier(id: number, d: Devis): Observable<Devis> { return this.http.put<Devis>(`${API}/devis/${id}`, d); }
  supprimer(id: number): Observable<void> { return this.http.delete<void>(`${API}/devis/${id}`); }
  envoyer(id: number): Observable<Devis> { return this.http.post<Devis>(`${API}/devis/${id}/envoi`, {}); }
  accepter(id: number): Observable<Devis> { return this.http.post<Devis>(`${API}/devis/${id}/acceptation`, {}); }
  refuser(id: number): Observable<Devis> { return this.http.post<Devis>(`${API}/devis/${id}/refus`, {}); }
}

@Injectable({ providedIn: 'root' })
export class FactureApi {
  private readonly http = inject(HttpClient);
  lister(statut: StatutFacture | '', page: number, size = 10): Observable<Page<Facture>> {
    return this.http.get<Page<Facture>>(`${API}/factures`, { params: params({ statut, page, size, sort: 'id,desc' }) });
  }
  trouver(id: number): Observable<Facture> { return this.http.get<Facture>(`${API}/factures/${id}`); }
  creerDepuisDevis(devisId: number): Observable<Facture> {
    return this.http.post<Facture>(`${API}/factures/depuis-devis/${devisId}`, {});
  }
  emettre(id: number, dateEmission?: string, dateEcheance?: string): Observable<Facture> {
    return this.http.post<Facture>(`${API}/factures/${id}/emission`, { dateEmission: dateEmission || null, dateEcheance: dateEcheance || null });
  }
  annuler(id: number): Observable<Facture> { return this.http.post<Facture>(`${API}/factures/${id}/annulation`, {}); }
  paiements(factureId: number): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(`${API}/factures/${factureId}/paiements`);
  }
  /** Le serveur renvoie la facture mise à jour (statut, reste à payer). */
  payer(factureId: number, p: Paiement): Observable<Facture> {
    return this.http.post<Facture>(`${API}/factures/${factureId}/paiements`, p);
  }
  balanceAgee(date?: string): Observable<BalanceAgee[]> {
    return this.http.get<BalanceAgee[]>(`${API}/factures/balance-agee`, { params: params({ date }) });
  }
}

@Injectable({ providedIn: 'root' })
export class RelanceApi {
  private readonly http = inject(HttpClient);
  lister(nature: NatureRelance | '', page: number, size = 10): Observable<Page<Relance>> {
    return this.http.get<Page<Relance>>(`${API}/relances`, { params: params({ nature, page, size, sort: 'id,desc' }) });
  }
  changerStatut(id: number, statut: StatutRelance, resultat?: string): Observable<Relance> {
    return this.http.patch<Relance>(`${API}/relances/${id}/statut`, { statut, resultat: resultat || null });
  }
  genererPaiement(date?: string): Observable<ResultatTraitement> {
    return this.http.post<ResultatTraitement>(`${API}/relances/paiement/generer`, {}, { params: params({ date }) });
  }
}

/**
 * Liste des clients (endpoint de P2). Contrat supposé : GET /api/clients?size=200 -> Page avec { id, raisonSociale }.
 * Si l'appel échoue, les formulaires basculent sur une saisie de l'identifiant à la main.
 */
@Injectable({ providedIn: 'root' })
export class ClientApi {
  private readonly http = inject(HttpClient);
  lister(): Observable<ClientResume[]> {
    return this.http.get<Page<ClientResume> | ClientResume[]>(`${API}/clients`, { params: params({ size: 200 }) })
      .pipe(map(r => Array.isArray(r) ? r : r.content));
  }
}

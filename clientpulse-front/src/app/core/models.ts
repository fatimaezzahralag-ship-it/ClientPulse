// Miroir TypeScript des DTO du backend (package ma.ensam.clientpulse.dto).
// Un BigDecimal Java arrive en JSON sous forme de nombre : on l'écrit "number".

export type StatutDevis = 'BROUILLON' | 'ENVOYE' | 'ACCEPTE' | 'REFUSE' | 'EXPIRE';
export type StatutFacture = 'BROUILLON' | 'EMISE' | 'PARTIELLEMENT_PAYEE' | 'PAYEE' | 'EN_RETARD' | 'ANNULEE';
export type TypeFacture = 'FACTURE' | 'AVOIR';
export type ModePaiement = 'VIREMENT' | 'CHEQUE' | 'ESPECES' | 'CARTE' | 'EFFET';
export type NatureRelance = 'PAIEMENT' | 'COMMERCIALE';
export type StatutRelance = 'A_FAIRE' | 'EN_COURS' | 'TERMINEE';

export const MODES_PAIEMENT: ModePaiement[] = ['VIREMENT', 'CHEQUE', 'ESPECES', 'CARTE', 'EFFET'];
export const STATUTS_RELANCE: StatutRelance[] = ['A_FAIRE', 'EN_COURS', 'TERMINEE'];

/** Réponse paginée de Spring Data (Page<T>). */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // page courante, à partir de 0
  size: number;
}

export interface TauxTva { id?: number; libelle: string; taux: number; actif: boolean; }

export interface Produit {
  id?: number;
  reference: string;
  designation: string;
  prixUnitaireHT: number;
  marge?: number | null;
  actif: boolean;
  tauxTvaId: number;
  tauxTva?: number; // rempli par le serveur
}

export interface Ligne {
  id?: number;
  produitId?: number | null;
  designation?: string;
  quantite: number;
  prixUnitaireHT?: number | null;
  remise?: number | null;
  tauxTVA?: number | null;
  montantHT?: number; // calculé par le serveur
  montantTVA?: number;
}

export interface Devis {
  id?: number;
  numero?: string;
  dateEmission?: string | null; // "2026-11-30"
  dateValidite?: string | null;
  statut?: StatutDevis;
  clientId: number;
  commercialId?: number | null;
  lignes: Ligne[];
  totalHT?: number;
  totalTVA?: number;
  totalTTC?: number;
}

export interface Paiement {
  id?: number;
  factureId?: number;
  datePaiement: string;
  montant: number;
  mode: ModePaiement;
  reference?: string | null;
}

export interface Facture {
  id: number;
  numero: string | null;
  type: TypeFacture;
  dateEmission: string | null;
  dateEcheance: string | null;
  statut: StatutFacture;
  clientId: number;
  devisId: number | null;
  factureOrigineId: number | null;
  lignes: Ligne[];
  paiements: Paiement[];
  totalHT: number;
  totalTVA: number;
  totalTTC: number;
  totalPaye: number;
  resteAPayer: number;
}

export interface BalanceAgee {
  clientId: number;
  nonEchu: number;
  retard1a30: number;
  retard31a60: number;
  retard61a90: number;
  retardPlusDe90: number;
}

export interface Relance {
  id: number;
  nature: NatureRelance;
  niveau: number;
  dateCreation: string;
  echeance: string | null;
  statut: StatutRelance;
  resultat: string | null;
  contenu: string | null;
  suggereeParSysteme: boolean;
  clientId: number;
  factureId: number | null;
  affecteAId: number | null;
}

export interface ResultatTraitement { facturesPasseesEnRetard: number; relancesCreees: number; }

/** Corps d'erreur renvoyé par GlobalExceptionHandler. */
export interface ErreurApi {
  horodatage: string;
  statut: number;
  erreur: string;
  message: string;
  details: Record<string, string> | null;
}

/** Client côté P2 : on ne lit que ce dont l'affichage a besoin. */
export interface ClientResume { id: number; raisonSociale?: string; code?: string; }

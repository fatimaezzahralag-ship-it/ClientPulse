export type NatureRelance = 'PAIEMENT' | 'COMMERCIALE';
export type StatutRelance = 'A_FAIRE' | 'EN_COURS' | 'TERMINEE';
export type ResultatRelance =
  | 'INTERESSE'
  | 'PAS_INTERESSE'
  | 'RENDEZ_VOUS_PRIS'
  | 'INJOIGNABLE'
  | 'SANS_REPONSE'
  | 'AUTRE';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Relance {
  id: number;
  clientId: number;
  codeClient: string;
  raisonSociale: string;
  commercialId: number | null;
  commercialNom: string | null;
  nature: NatureRelance;
  statut: StatutRelance;
  motif: string;
  resultat: ResultatRelance | null;
  commentaire: string | null;
  dateEcheance: string | null;
  dateCreation: string;
}

export interface RelanceFiltres {
  statut: StatutRelance | '';
  page: number;
  size: number;
}

export const STATUTS: StatutRelance[] = [
  'A_FAIRE',
  'EN_COURS',
  'TERMINEE'
];

export const RESULTATS: ResultatRelance[] = [
  'INTERESSE',
  'PAS_INTERESSE',
  'RENDEZ_VOUS_PRIS',
  'INJOIGNABLE',
  'SANS_REPONSE',
  'AUTRE'
];

export const LIBELLES_STATUTS: Record<StatutRelance, string> = {
  A_FAIRE: 'À faire',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée'
};

export const LIBELLES_RESULTATS: Record<ResultatRelance, string> = {
  INTERESSE: 'Intéressé',
  PAS_INTERESSE: 'Pas intéressé',
  RENDEZ_VOUS_PRIS: 'Rendez-vous pris',
  INJOIGNABLE: 'Injoignable',
  SANS_REPONSE: 'Sans réponse',
  AUTRE: 'Autre'
};

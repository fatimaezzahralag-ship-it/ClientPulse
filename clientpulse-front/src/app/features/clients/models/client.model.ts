export type TypeClient = 'ENTREPRISE' | 'PARTICULIER';

export type StatutClient = 'PROSPECT' | 'ACTIF' | 'INACTIF';

export type NiveauRisque = 'FAIBLE' | 'MOYEN' | 'ELEVE' | 'CRITIQUE';

export type SegmentRFM =
  | 'CHAMPION'
  | 'LOYAL'
  | 'NOUVEAU'
  | 'A_RISQUE'
  | 'HIBERNATION'
  | 'PERDU';

/** Réponse paginée Spring Data (Page<T>). */
export interface PageResponse<T> {

  content: T[];

  totalElements: number;

  totalPages: number;

  number: number;

  size: number;

  first: boolean;

  last: boolean;
}

export interface Client {

  id: number;

  code: string;

  raisonSociale: string;

  ice: string | null;

  type: TypeClient;

  email: string | null;

  telephone: string | null;

  adresse: string | null;

  ville: string | null;

  secteur: string | null;

  delaiPaiementJours: number | null;

  statut: StatutClient;

  commercialId: number | null;
}

export interface ClientRequest {

  code: string;

  raisonSociale: string;

  ice: string | null;

  type: TypeClient;

  email: string | null;

  telephone: string | null;

  adresse: string | null;

  ville: string | null;

  secteur: string | null;

  delaiPaiementJours: number;

  statut: StatutClient;

  commercialId: number | null;
}

export interface Commercial {

  id: number;

  nom: string | null;

  prenom: string | null;

  email: string | null;

  equipe: string | null;
}

export interface ClientFiltres {

  keyword?: string;

  statut?: StatutClient | '';

  type?: TypeClient | '';

  ville?: string;

  commercialId?: number | null;

  page: number;

  size: number;
}

export interface ScoreClient {

  id: number;

  clientId: number;

  dateCalcul: string;

  scoreRisque: number;

  niveau: NiveauRisque;

  segment: SegmentRFM;

  facteursExplicatifs: string[];
}

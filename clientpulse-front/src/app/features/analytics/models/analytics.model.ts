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

export interface ScoreClient {
  id: number;
  clientId: number;
  dateCalcul: string;
  scoreRisque: number;
  niveau: NiveauRisque;
  segment: SegmentRFM;
  facteursExplicatifs: string[];
}

export interface ClientARelancer {
  clientId: number;
  code: string;
  raisonSociale: string;
  scoreRisque: number;
  niveau: NiveauRisque;
  segment: SegmentRFM;
  facteursExplicatifs: string[];
}

export interface AnalyticsDashboard {
  clientsEvalues: number;
  clientsARisque: number;
  repartitionNiveaux: Record<string, number>;
  repartitionSegments: Record<string, number>;
}

export interface ParametreScoring {
  id: number;
  nom: string;
  valeur: number;
}

export const LIBELLES_NIVEAUX: Record<NiveauRisque, string> = {
  FAIBLE: 'Faible',
  MOYEN: 'Moyen',
  ELEVE: 'Élevé',
  CRITIQUE: 'Critique'
};

export const LIBELLES_SEGMENTS: Record<SegmentRFM, string> = {
  CHAMPION: 'Champion',
  LOYAL: 'Fidèle',
  NOUVEAU: 'Nouveau',
  A_RISQUE: 'À risque',
  HIBERNATION: 'En hibernation',
  PERDU: 'Perdu'
};

export const NIVEAUX: NiveauRisque[] = [
  'FAIBLE',
  'MOYEN',
  'ELEVE',
  'CRITIQUE'
];

export const SEGMENTS: SegmentRFM[] = [
  'CHAMPION',
  'LOYAL',
  'NOUVEAU',
  'A_RISQUE',
  'HIBERNATION',
  'PERDU'
];

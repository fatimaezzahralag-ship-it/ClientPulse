export type TypeInteraction =
  | 'APPEL'
  | 'EMAIL'
  | 'REUNION'
  | 'RECLAMATION';

export type StatutInteraction = 'OUVERTE' | 'EN_COURS' | 'CLOTUREE';

export interface Interaction {

  id: number;

  type: TypeInteraction;

  /** Date ISO renvoyée par le backend (LocalDateTime). */
  date: string;

  resume: string;

  statut: StatutInteraction;

  clientId: number;
}

export interface InteractionRequest {

  type: TypeInteraction;

  date: string;

  resume: string;

  statut: StatutInteraction;
}

export const LIBELLES_TYPE_INTERACTION: Record<TypeInteraction, string> = {
  APPEL: 'Appel',
  EMAIL: 'E-mail',
  REUNION: 'Réunion',
  RECLAMATION: 'Réclamation'
};

export const LIBELLES_STATUT_INTERACTION: Record<StatutInteraction, string> = {
  OUVERTE: 'Ouverte',
  EN_COURS: 'En cours',
  CLOTUREE: 'Clôturée'
};

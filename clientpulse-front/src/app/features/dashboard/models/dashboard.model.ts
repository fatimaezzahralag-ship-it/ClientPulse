/** Reflet de DashboardDto (GET /api/dashboard). Montants en MAD, taux en %. */
export interface PointMensuel {
  mois: string;      // AAAA-MM
  facture: number;
  encaisse: number;
}

export interface TrancheEncours {
  tranche: string;
  montant: number;
  pourcentage: number;
}

export interface ClientCa {
  clientId: number;
  raisonSociale: string;
  chiffreAffairesHt: number;
}

export interface Dashboard {
  dateReference: string;
  periodeMois: number;

  clientsDebutPeriode: number;
  clientsFinPeriode: number;
  nouveauxClients: number;
  tauxRetention: number;
  tauxChurnAnnuel: number;

  margeMoyenneParFacture: number;
  facturesParClientParAn: number;
  dureeVieAnnees: number;
  clv: number;

  chiffreAffairesHt: number;
  totalFactureTtc: number;
  totalEncaisse: number;
  encours: number;
  encoursEchu: number;
  dso: number;
  tauxRecouvrement: number;

  relancesEffectuees: number;
  relancesReussies: number;
  tauxSuccesRelances: number;

  serieMensuelle: PointMensuel[];
  encoursParTranche: TrancheEncours[];
  meilleursClients: ClientCa[];
  repartitionSegments: Record<string, number>;
  repartitionNiveaux: Record<string, number>;
}

export const PERIODES_DASHBOARD = [
  { mois: 3, libelle: '3 mois' },
  { mois: 6, libelle: '6 mois' },
  { mois: 12, libelle: '12 mois' }
] as const;

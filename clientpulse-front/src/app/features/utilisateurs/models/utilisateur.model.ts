export type RoleNom =
  | 'ADMIN'
  | 'MANAGER'
  | 'COMMERCIAL'
  | 'COMPTABLE'
  | 'ANALYSTE';

export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  actif: boolean;
  roles: string[];
}

export interface UtilisateurRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse?: string;
  roles: string[];
}

export const ROLES: RoleNom[] = [
  'ADMIN',
  'MANAGER',
  'COMMERCIAL',
  'COMPTABLE',
  'ANALYSTE'
];

export const LIBELLES_ROLES: Record<RoleNom, string> = {
  ADMIN: 'Administrateur',
  MANAGER: 'Manager',
  COMMERCIAL: 'Commercial',
  COMPTABLE: 'Comptable',
  ANALYSTE: 'Analyste'
};

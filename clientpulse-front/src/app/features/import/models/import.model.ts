/** Reflets de RapportImportDto, LigneRejeteeDto et JournalImportDto (/api/import). */
export interface LigneRejetee {
  numeroLigne: number;
  code: string;
  motif: string;
}

export interface RapportImport {
  journalId: number;
  fichier: string;
  lignesLues: number;
  lignesImportees: number;
  lignesRejetees: number;
  rejets: LigneRejetee[];
}

export interface JournalImport {
  id: number;
  fichier: string;
  lignesLues: number;
  lignesImportees: number;
  lignesRejetees: number;
  date: string | null;
  creePar: string | null;
}

export interface PageJournalImport {
  content: JournalImport[];
  totalElements: number;
}

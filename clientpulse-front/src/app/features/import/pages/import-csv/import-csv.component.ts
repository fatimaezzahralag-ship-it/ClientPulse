import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { AlertComponent } from '../../../../shared/ui/alert/alert.component';
import { EmptyStateComponent } from '../../../../shared/ui/state/empty-state.component';
import { PageHeaderComponent } from '../../../../shared/ui/page-header/page-header.component';
import { JournalImport, RapportImport } from '../../models/import.model';
import { ImportService } from '../../services/import.service';

const TAILLE_MAX = 5 * 1024 * 1024;   // identique à spring.servlet.multipart.max-file-size

/** Import CSV de clients (ADMIN) : choix du fichier, rapport de contrôle, historique. */
@Component({
  selector: 'app-import-csv',
  imports: [DatePipe, AlertComponent, EmptyStateComponent, PageHeaderComponent],
  templateUrl: './import-csv.component.html',
  styleUrl: './import-csv.component.css'
})
export class ImportCsvComponent implements OnInit {

  private readonly service = inject(ImportService);

  protected readonly fichier = signal<File | null>(null);
  protected readonly enCours = signal(false);
  protected readonly rapport = signal<RapportImport | null>(null);
  protected readonly erreur = signal('');
  protected readonly journal = signal<JournalImport[]>([]);

  ngOnInit(): void {
    this.chargerJournal();
  }

  protected choisirFichier(evenement: Event): void {
    const champ = evenement.target as HTMLInputElement;
    const choisi = champ.files?.item(0) ?? null;
    this.rapport.set(null);
    this.erreur.set('');

    if (choisi && !choisi.name.toLowerCase().endsWith('.csv')) {
      this.fichier.set(null);
      this.erreur.set('Le fichier doit être au format CSV (.csv).');
      champ.value = '';
      return;
    }
    if (choisi && choisi.size > TAILLE_MAX) {
      this.fichier.set(null);
      this.erreur.set('Le fichier dépasse la taille maximale de 5 Mo.');
      champ.value = '';
      return;
    }
    this.fichier.set(choisi);
  }

  protected importer(): void {
    const choisi = this.fichier();
    if (!choisi || this.enCours()) {
      return;
    }
    this.enCours.set(true);
    this.erreur.set('');
    this.rapport.set(null);

    this.service.importerClients(choisi).subscribe({
      next: (rapport) => {
        this.rapport.set(rapport);
        this.enCours.set(false);
        this.chargerJournal();
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(e.error?.detail ?? e.error?.message ?? 'L’import a échoué.');
        this.enCours.set(false);
      }
    });
  }

  protected telechargerModele(): void {
    this.service.telechargerModele().subscribe({
      error: () => this.erreur.set('Impossible de télécharger le modèle.')
    });
  }

  private chargerJournal(): void {
    this.service.journal().subscribe({
      next: (page) => this.journal.set(page.content ?? []),
      error: () => this.journal.set([])
    });
  }
}

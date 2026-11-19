import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { debounceTime } from 'rxjs';

import {
  Client,
  ClientFiltres,
  Commercial,
  StatutClient,
  TypeClient
} from '../../models/client.model';
import { ClientService } from '../../services/client.service';

@Component({
  selector: 'app-client-list',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './client-list.component.html',
  styleUrl: './client-list.component.css'
})
export class ClientListComponent implements OnInit {

  private readonly clientService = inject(ClientService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly taillePage = 10;

  protected readonly filtres = this.fb.nonNullable.group({
    keyword: [''],
    statut: [''],
    type: [''],
    ville: [''],
    commercialId: ['']
  });

  protected readonly clients = signal<Client[]>([]);
  protected readonly commerciaux = signal<Commercial[]>([]);
  protected readonly chargement = signal(false);
  protected readonly erreur = signal('');
  protected readonly page = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);

  protected readonly nomsCommerciaux = computed(() => {
    const noms = new Map<number, string>();
    for (const commercial of this.commerciaux()) {
      const nom = `${commercial.prenom ?? ''} ${commercial.nom ?? ''}`.trim();
      noms.set(commercial.id, nom || `Commercial #${commercial.id}`);
    }
    return noms;
  });

  ngOnInit(): void {
    this.chargerCommerciaux();

    this.filtres.valueChanges
      .pipe(debounceTime(350), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.page.set(0);
        this.chargerClients();
      });

    this.chargerClients();
  }

  protected chargerClients(): void {

    const valeurs = this.filtres.getRawValue();

    const filtres: ClientFiltres = {
      keyword: valeurs.keyword,
      statut: valeurs.statut as StatutClient | '',
      type: valeurs.type as TypeClient | '',
      ville: valeurs.ville,
      commercialId: valeurs.commercialId
        ? Number(valeurs.commercialId)
        : null,
      page: this.page(),
      size: this.taillePage
    };

    this.chargement.set(true);
    this.erreur.set('');

    this.clientService.rechercher(filtres).subscribe({
      next: (reponse) => {
        this.clients.set(reponse.content);
        this.totalElements.set(reponse.totalElements);
        this.totalPages.set(reponse.totalPages);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set(
          'Impossible de charger la liste des clients. Vérifiez que le backend est démarré.'
        );
        this.chargement.set(false);
      }
    });
  }

  protected reinitialiser(): void {
    this.filtres.reset({
      keyword: '',
      statut: '',
      type: '',
      ville: '',
      commercialId: ''
    });
    this.page.set(0);
    this.chargerClients();
  }

  protected pagePrecedente(): void {
    if (this.page() > 0) {
      this.page.update((page) => page - 1);
      this.chargerClients();
    }
  }

  protected pageSuivante(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((page) => page + 1);
      this.chargerClients();
    }
  }

  protected nomCommercial(client: Client): string {
    if (client.commercialId == null) {
      return 'Non affecté';
    }
    return this.nomsCommerciaux().get(client.commercialId)
      ?? `Commercial #${client.commercialId}`;
  }

  protected libelleType(type: TypeClient): string {
    return type === 'ENTREPRISE' ? 'Entreprise' : 'Particulier';
  }

  protected classeStatut(statut: StatutClient): string {
    switch (statut) {
      case 'ACTIF':
        return 'badge badge-active';
      case 'PROSPECT':
        return 'badge badge-prospect';
      default:
        return 'badge badge-inactive';
    }
  }

  private chargerCommerciaux(): void {
    this.clientService.commerciaux().subscribe({
      next: (commerciaux) => this.commerciaux.set(commerciaux),
      error: () => this.commerciaux.set([])
    });
  }
}

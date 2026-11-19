import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import {
  ClientRequest,
  Commercial,
  StatutClient,
  TypeClient
} from '../../models/client.model';
import { ClientService } from '../../services/client.service';

@Component({
  selector: 'app-client-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './client-form.component.html',
  styleUrl: './client-form.component.css'
})
export class ClientFormComponent implements OnInit {

  private readonly clientService = inject(ClientService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly id = signal<number | null>(null);
  protected readonly edition = signal(false);
  protected readonly chargement = signal(false);
  protected readonly enregistrement = signal(false);
  protected readonly erreur = signal('');
  protected readonly commerciaux = signal<Commercial[]>([]);

  protected readonly formulaire = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.maxLength(30)]],
    raisonSociale: ['', [Validators.required, Validators.maxLength(150)]],
    ice: ['', [Validators.maxLength(20)]],
    type: ['ENTREPRISE' as TypeClient, [Validators.required]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    telephone: ['', [Validators.maxLength(30)]],
    adresse: ['', [Validators.maxLength(255)]],
    ville: ['', [Validators.maxLength(80)]],
    secteur: ['', [Validators.maxLength(80)]],
    delaiPaiementJours: [
      30,
      [Validators.required, Validators.min(0), Validators.max(180)]
    ],
    statut: ['PROSPECT' as StatutClient, [Validators.required]],
    commercialId: ['']
  });

  ngOnInit(): void {

    this.chargerCommerciaux();

    const parametre = this.route.snapshot.paramMap.get('id');
    if (!parametre) {
      return;
    }

    const identifiant = Number(parametre);
    if (Number.isNaN(identifiant)) {
      void this.router.navigate(['/clients']);
      return;
    }

    this.id.set(identifiant);
    this.edition.set(true);
    this.chargerClient(identifiant);
  }

  protected soumettre(): void {

    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.enregistrement.set(true);
    this.erreur.set('');

    const requete = this.construireRequete();
    const identifiant = this.id();

    const operation = identifiant
      ? this.clientService.modifier(identifiant, requete)
      : this.clientService.creer(requete);

    operation.subscribe({
      next: (client) => {
        this.enregistrement.set(false);
        void this.router.navigate(['/clients', client.id]);
      },
      error: (erreur: unknown) => {
        this.enregistrement.set(false);
        this.erreur.set(this.messageDErreur(erreur));
      }
    });
  }

  protected annuler(): void {
    const identifiant = this.id();
    void this.router.navigate(
      identifiant ? ['/clients', identifiant] : ['/clients']
    );
  }

  private chargerClient(id: number): void {
    this.chargement.set(true);
    this.clientService.trouver(id).subscribe({
      next: (client) => {
        this.formulaire.patchValue({
          code: client.code,
          raisonSociale: client.raisonSociale,
          ice: client.ice ?? '',
          type: client.type,
          email: client.email ?? '',
          telephone: client.telephone ?? '',
          adresse: client.adresse ?? '',
          ville: client.ville ?? '',
          secteur: client.secteur ?? '',
          delaiPaiementJours: client.delaiPaiementJours ?? 30,
          statut: client.statut,
          commercialId: client.commercialId?.toString() ?? ''
        });
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Client introuvable.');
        this.chargement.set(false);
      }
    });
  }

  private chargerCommerciaux(): void {
    this.clientService.commerciaux().subscribe({
      next: (commerciaux) => this.commerciaux.set(commerciaux),
      error: () => this.commerciaux.set([])
    });
  }

  private construireRequete(): ClientRequest {

    const valeurs = this.formulaire.getRawValue();

    return {
      code: valeurs.code.trim(),
      raisonSociale: valeurs.raisonSociale.trim(),
      ice: texteOuNull(valeurs.ice),
      type: valeurs.type,
      email: texteOuNull(valeurs.email),
      telephone: texteOuNull(valeurs.telephone),
      adresse: texteOuNull(valeurs.adresse),
      ville: texteOuNull(valeurs.ville),
      secteur: texteOuNull(valeurs.secteur),
      delaiPaiementJours: Number(valeurs.delaiPaiementJours),
      statut: valeurs.statut,
      commercialId: valeurs.commercialId
        ? Number(valeurs.commercialId)
        : null
    };
  }

  private messageDErreur(erreur: unknown): string {
    if (erreur instanceof HttpErrorResponse) {
      const detail = erreur.error?.detail;
      if (typeof detail === 'string' && detail.length > 0) {
        return detail;
      }
      if (erreur.status === 409) {
        return 'Un client possède déjà ce code.';
      }
    }
    return "L'enregistrement a échoué. Vérifiez les champs saisis.";
  }
}

function texteOuNull(valeur: string): string | null {
  const propre = valeur.trim();
  return propre.length > 0 ? propre : null;
}

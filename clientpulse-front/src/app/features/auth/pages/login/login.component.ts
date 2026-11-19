import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  protected readonly formulaire = this.fb.nonNullable.group({
    email: ['admin@clientpulse.ma', [Validators.required, Validators.email]],
    motDePasse: ['Admin@12345', [Validators.required]]
  });

  protected chargement = false;
  protected erreur = '';

  ngOnInit(): void {
    if (this.auth.connecte()) {
      void this.router.navigate(['/clients']);
    }
  }

  protected soumettre(): void {

    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.chargement = true;
    this.erreur = '';

    const { email, motDePasse } = this.formulaire.getRawValue();

    this.auth.connexion(email, motDePasse).subscribe({
      next: () => {
        this.chargement = false;
        const retour = this.route.snapshot.queryParamMap.get('retour');
        const destination = retour && retour.startsWith('/')
          ? retour
          : '/clients';
        void this.router.navigateByUrl(destination);
      },
      error: (erreur: unknown) => {
        this.chargement = false;
        this.erreur = this.messageDErreur(erreur);
      }
    });
  }

  private messageDErreur(erreur: unknown): string {
    if (erreur instanceof HttpErrorResponse) {
      const detail = erreur.error?.detail;
      if (typeof detail === 'string' && detail.length > 0) {
        return detail;
      }
      if (erreur.status === 401) {
        return 'Email ou mot de passe incorrect.';
      }
      if (erreur.status === 0) {
        return 'Le serveur ClientPulse est injoignable (port 8080).';
      }
    }
    return 'Connexion impossible. Veuillez réessayer.';
  }
}

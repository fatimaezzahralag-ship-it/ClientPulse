import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ParametresComponent } from './parametres.component';

const PARAMETRES = [
  { id: 1, nom: 'POIDS_INACTIVITE', valeur: 0.3 },
  { id: 2, nom: 'POIDS_BAISSE_ACHATS', valeur: 0.25 },
  { id: 3, nom: 'POIDS_RECLAMATIONS', valeur: 0.15 },
  { id: 4, nom: 'POIDS_RETARD_PAIEMENT', valeur: 0.3 },
  { id: 5, nom: 'SEUIL_RISQUE_ELEVE', valeur: 70 },
  { id: 6, nom: 'DELAI_RELANCE_N1', valeur: 3 },
  { id: 7, nom: 'DELAI_RELANCE_N2', valeur: 3 }
];

describe('ParametresComponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParametresComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function demarrer() {
    const fixture = TestBed.createComponent(ParametresComponent);
    fixture.detectChanges();
    http.expectOne('/api/parametres').flush(PARAMETRES);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture;
  }

  it('regroupe les paramètres par thème, délais de relance en premier', async () => {
    const fixture = await demarrer();

    const titres = Array.from(fixture.nativeElement.querySelectorAll('h2')).map((h) => (h as HTMLElement).textContent);
    expect(titres).toEqual(['Délais des relances de paiement', 'Poids du score de risque', 'Niveaux de risque']);
    expect(fixture.nativeElement.textContent).toContain('Niveau 1 : rappel préventif');
  });

  it('contrôle que la somme des poids fait 1', async () => {
    const fixture = await demarrer();
    expect(fixture.nativeElement.textContent).toContain('Somme des poids : 1.00');
    expect(fixture.nativeElement.querySelector('.somme-ko')).toBeNull();
  });

  it('enregistre un paramètre modifié', async () => {
    const fixture = await demarrer();
    const champ = fixture.nativeElement.querySelector('input[aria-label="Niveau 2 : rappel amiable"]') as HTMLInputElement;
    champ.value = '5';
    champ.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const ligne = champ.closest('tr') as HTMLElement;
    const bouton = ligne.querySelector('button') as HTMLButtonElement;
    expect(bouton.disabled).toBe(false);
    bouton.click();

    const requete = http.expectOne('/api/parametres/7');
    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body).toEqual({ valeur: 5 });
    requete.flush({ id: 7, nom: 'DELAI_RELANCE_N2', valeur: 5 });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('enregistré');
  });

  it('désactive l’enregistrement d’une saisie invalide', async () => {
    const fixture = await demarrer();
    const champ = fixture.nativeElement.querySelector('input[aria-label="Niveau 2 : rappel amiable"]') as HTMLInputElement;
    champ.value = 'abc';
    champ.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect((champ.closest('tr')!.querySelector('button') as HTMLButtonElement).disabled).toBe(true);
  });
});

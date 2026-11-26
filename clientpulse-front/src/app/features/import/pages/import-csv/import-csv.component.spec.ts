import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ImportCsvComponent } from './import-csv.component';

function choisir(fixture: { nativeElement: HTMLElement }, fichier: File): HTMLInputElement {
  const champ = fixture.nativeElement.querySelector('input[type=file]') as HTMLInputElement;
  Object.defineProperty(champ, 'files', {
    configurable: true,
    value: { length: 1, item: (i: number) => (i === 0 ? fichier : null) }
  });
  champ.dispatchEvent(new Event('change'));
  return champ;
}

describe('ImportCsvComponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportCsvComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function demarrer() {
    const fixture = TestBed.createComponent(ImportCsvComponent);
    fixture.detectChanges();
    http.expectOne((r) => r.url === '/api/import/journal').flush({ content: [], totalElements: 0 });
    fixture.detectChanges();
    return fixture;
  }

  it('refuse un fichier qui n’est pas un CSV sans appeler l’API', () => {
    const fixture = demarrer();

    choisir(fixture, new File(['x'], 'notes.pdf'));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('format CSV');
    const importer = fixture.nativeElement.querySelector('button.btn-primary') as HTMLButtonElement;
    expect(importer.disabled).toBe(true);
  });

  it('importe le fichier et affiche le compte rendu avec les rejets', () => {
    const fixture = demarrer();
    choisir(fixture, new File(['code;raison_sociale;type'], 'clients.csv', { type: 'text/csv' }));
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('button.btn-primary') as HTMLButtonElement).click();

    http.expectOne('/api/import/clients').flush({
      journalId: 1,
      fichier: 'clients.csv',
      lignesLues: 3,
      lignesImportees: 2,
      lignesRejetees: 1,
      rejets: [{ numeroLigne: 4, code: 'CLI-3', motif: 'L’adresse e-mail est invalide.' }]
    });
    http.expectOne((r) => r.url === '/api/import/journal').flush({ content: [], totalElements: 0 });
    fixture.detectChanges();

    const texte: string = fixture.nativeElement.textContent;
    expect(texte).toContain('clients importés');
    expect(texte).toContain('CLI-3');
    expect(texte).toContain('e-mail est invalide');
  });

  it('affiche le message du serveur quand l’import échoue', () => {
    const fixture = demarrer();
    choisir(fixture, new File(['x'], 'clients.csv'));
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('button.btn-primary') as HTMLButtonElement).click();
    http.expectOne('/api/import/clients').flush(
      { detail: 'Colonnes obligatoires manquantes : type' },
      { status: 422, statusText: 'Unprocessable Entity' }
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Colonnes obligatoires manquantes');
  });
});

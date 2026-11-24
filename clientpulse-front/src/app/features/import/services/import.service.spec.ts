import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ImportService } from './import.service';

describe('ImportService', () => {
  let service: ImportService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ImportService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envoie le fichier dans le champ « fichier » du formulaire', () => {
    const fichier = new File(['code;raison_sociale;type'], 'clients.csv', { type: 'text/csv' });
    service.importerClients(fichier).subscribe();

    const requete = http.expectOne('/api/import/clients');
    expect(requete.request.method).toBe('POST');
    const corps = requete.request.body as FormData;
    expect((corps.get('fichier') as File).name).toBe('clients.csv');
    requete.flush({});
  });

  it('lit la première page de l’historique', () => {
    service.journal().subscribe();

    const requete = http.expectOne((r) => r.url === '/api/import/journal');
    expect(requete.request.params.get('page')).toBe('0');
    expect(requete.request.params.get('size')).toBe('10');
    requete.flush({ content: [], totalElements: 0 });
  });
});

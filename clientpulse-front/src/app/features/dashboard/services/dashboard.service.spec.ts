import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  let service: DashboardService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(DashboardService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('demande les indicateurs de la période choisie', () => {
    service.indicateurs(6).subscribe();

    const requete = http.expectOne((r) => r.url === '/api/dashboard');
    expect(requete.request.method).toBe('GET');
    expect(requete.request.params.get('periode')).toBe('6');
    requete.flush({});
  });
});

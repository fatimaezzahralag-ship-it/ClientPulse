import { Component, input } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ChartComponent } from '../../../../shared/ui/chart/chart.component';
import { Dashboard } from '../../models/dashboard.model';
import { KpiDashboardComponent } from './kpi-dashboard.component';

/** Chart.js a besoin d'un vrai canvas : on le remplace par une doublure dans ces tests. */
@Component({ selector: 'app-chart', template: '' })
class ChartDoublure {
  readonly type = input<string>();
  readonly data = input<unknown>();
  readonly options = input<unknown>();
}

function indicateurs(surcharge: Partial<Dashboard> = {}): Dashboard {
  return {
    dateReference: '2026-11-19',
    periodeMois: 12,
    clientsDebutPeriode: 10,
    clientsFinPeriode: 12,
    nouveauxClients: 4,
    tauxRetention: 80,
    tauxChurnAnnuel: 20,
    margeMoyenneParFacture: 100,
    facturesParClientParAn: 2,
    dureeVieAnnees: 5,
    clv: 1000,
    chiffreAffairesHt: 250000,
    totalFactureTtc: 300000,
    totalEncaisse: 200000,
    encours: 100000,
    encoursEchu: 40000,
    dso: 37.5,
    tauxRecouvrement: 75,
    relancesEffectuees: 20,
    relancesReussies: 5,
    tauxSuccesRelances: 25,
    serieMensuelle: [{ mois: '2026-11', facture: 1000, encaisse: 800 }],
    encoursParTranche: [
      { tranche: 'Non échu', montant: 60000, pourcentage: 60 },
      { tranche: 'Plus de 90 j', montant: 40000, pourcentage: 40 }
    ],
    meilleursClients: [{ clientId: 1, raisonSociale: 'Atlas SARL', chiffreAffairesHt: 90000 }],
    repartitionSegments: { CHAMPION: 3 },
    repartitionNiveaux: { FAIBLE: 5 },
    ...surcharge
  };
}

describe('KpiDashboardComponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KpiDashboardComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
      .overrideComponent(KpiDashboardComponent, {
        remove: { imports: [ChartComponent] },
        add: { imports: [ChartDoublure] }
      })
      .compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const requetePeriode = (mois: number) =>
    http.expectOne((r) => r.url === '/api/dashboard' && r.params.get('periode') === String(mois));

  it('charge 12 mois au démarrage et affiche les indicateurs', async () => {
    const fixture = TestBed.createComponent(KpiDashboardComponent);
    fixture.detectChanges();
    requetePeriode(12).flush(indicateurs());
    fixture.detectChanges();
    await fixture.whenStable();

    const texte: string = fixture.nativeElement.textContent;
    expect(texte).toContain('Tableau de bord');
    expect(texte).toContain('80 %');            // rétention
    expect(texte).toContain('37,5 j');          // DSO
    expect(texte).toContain('Plus de 90 j');    // tableau de l'encours
  });

  it('recharge les données quand on change de période', () => {
    const fixture = TestBed.createComponent(KpiDashboardComponent);
    fixture.detectChanges();
    requetePeriode(12).flush(indicateurs());
    fixture.detectChanges();

    const boutons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('.periodes button'));
    boutons.find((b) => b.textContent?.includes('3 mois'))!.click();

    requetePeriode(3).flush(indicateurs({ periodeMois: 3 }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('période de 3 mois');
  });

  it('affiche un message clair quand le rôle est refusé', () => {
    const fixture = TestBed.createComponent(KpiDashboardComponent);
    fixture.detectChanges();
    requetePeriode(12).flush({}, { status: 403, statusText: 'Forbidden' });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('ne donne pas accès');
  });
});

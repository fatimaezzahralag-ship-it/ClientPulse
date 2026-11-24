import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import { FileDownloadService } from '../../../core/files/file-download.service';
import { Dashboard } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {

  private readonly http = inject(HttpClient);
  private readonly fichiers = inject(FileDownloadService);

  indicateurs(periodeMois: number): Observable<Dashboard> {
    const params = new HttpParams().set('periode', periodeMois);
    return this.http.get<Dashboard>('/api/dashboard', { params });
  }

  /** Export CSV des indicateurs (téléchargement dans le navigateur). */
  exporter(periodeMois: number) {
    return this.fichiers.telecharger(
      `/api/dashboard/export?periode=${periodeMois}`,
      'tableau-de-bord.csv'
    );
  }
}

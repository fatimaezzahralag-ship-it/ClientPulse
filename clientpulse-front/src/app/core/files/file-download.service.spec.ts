import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { describe, expect, it } from 'vitest';

import { FileDownloadService } from './file-download.service';

describe('FileDownloadService', () => {
  const service = (() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    return TestBed.inject(FileDownloadService);
  })();

  it('lit le nom du fichier dans Content-Disposition', () => {
    expect(service.nomDepuisEntete('inline; filename="facture-FAC-2026-0001.pdf"')).toBe('facture-FAC-2026-0001.pdf');
    expect(service.nomDepuisEntete('attachment; filename=export.csv')).toBe('export.csv');
  });

  it('gère la forme encodée filename*', () => {
    expect(service.nomDepuisEntete("attachment; filename*=UTF-8''d%C3%A9mo.csv")).toBe('démo.csv');
  });

  it('renvoie null sans en-tête', () => {
    expect(service.nomDepuisEntete(null)).toBeNull();
  });
});

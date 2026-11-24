import { Pipe, PipeTransform } from '@angular/core';

const FORMAT = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

/** 12500.5 -> « 12 500,50 MAD ». Valeur absente : « - ». */
@Pipe({ name: 'mad' })
export class MadPipe implements PipeTransform {
  transform(valeur: number | null | undefined, unite = 'MAD'): string {
    if (valeur === null || valeur === undefined || Number.isNaN(valeur)) {
      return '-';
    }
    return `${FORMAT.format(valeur)} ${unite}`.trim();
  }
}

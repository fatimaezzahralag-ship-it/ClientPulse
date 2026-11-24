import { describe, expect, it } from 'vitest';

import { MadPipe } from './mad.pipe';

describe('MadPipe', () => {
  const pipe = new MadPipe();

  it('formate un montant avec deux décimales et l’unité', () => {
    const resultat = pipe.transform(12500.5).replace(/\s/g, ' ');
    expect(resultat).toMatch(/^12 500,50 MAD$/);
  });

  it('affiche un tiret quand la valeur est absente', () => {
    expect(pipe.transform(null)).toBe('-');
    expect(pipe.transform(undefined)).toBe('-');
  });

  it('accepte une autre unité', () => {
    expect(pipe.transform(3, 'jours')).toContain('jours');
  });
});

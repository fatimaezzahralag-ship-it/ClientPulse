import { Component, computed, input } from '@angular/core';

export type TypeAlerte = 'error' | 'success' | 'info';

/** Bandeau de message : n'affiche rien quand le message est vide. */
@Component({
  selector: 'app-alert',
  template: `
    @if (message()) {
      <div [class]="classe()" role="alert">{{ message() }}</div>
    }
  `
})
export class AlertComponent {
  readonly message = input<string>('');
  readonly type = input<TypeAlerte>('info');

  protected readonly classe = computed(() => `alert alert-${this.type()}`);
}

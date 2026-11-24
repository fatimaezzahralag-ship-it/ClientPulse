import { Component, inject } from '@angular/core';
import { NotificationService } from '../core/notification.service';

@Component({
  host: { class: 'p1' },
  selector: 'app-notifications',
  template: `
    <div class="notifs">
      @for (m of service.messages(); track m.id) {
        <div class="notif" [class.erreur]="m.type === 'erreur'" (click)="service.fermer(m.id)">{{ m.texte }}</div>
      }
    </div>`,
})
export class Notifications {
  protected readonly service = inject(NotificationService);
}

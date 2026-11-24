import { Injectable, signal } from '@angular/core';

export interface Message { id: number; type: 'succes' | 'erreur'; texte: string; }

/** Petits messages en haut de l'écran. Affichés par le composant Notifications. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private suivant = 1;
  readonly messages = signal<Message[]>([]);

  succes(texte: string): void { this.ajouter('succes', texte); }
  erreur(texte: string): void { this.ajouter('erreur', texte); }
  fermer(id: number): void { this.messages.update(l => l.filter(m => m.id !== id)); }

  private ajouter(type: Message['type'], texte: string): void {
    const id = this.suivant++;
    this.messages.update(l => [...l, { id, type, texte }]);
    setTimeout(() => this.fermer(id), 6000);
  }
}

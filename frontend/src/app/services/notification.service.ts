// src/app/services/notification.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, firstValueFrom, throwError } from 'rxjs';
import { Notification } from '../models/notification.model';
import { AuthService } from './auth.services';

export interface NotificationRequest {
  titre: string;
  message: string;
  tempsRelatif?: string;
  couleur?: string;
  estLu?: boolean;
  idTontine: number;
  idUser: number;
  dateCreation?: string;
  statutNotification?: string;
  typeNotification?: string;
  lienAction?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';
  private tontinesUrl = 'http://localhost:8080/api/tontine/mes-tontines';
  private currentTontineStorageKey = 'currentNotificationTontineId';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Charge toutes les notifications de l'utilisateur courant,
   * toutes tontines confondues.
   * Tente d'abord l'endpoint direct /user/{id}, puis agrège
   * depuis chaque tontine si non disponible.
   */
  async getAllUserNotifications(): Promise<Notification[]> {
    const userId = this.getCurrentUserId();
    if (!userId) throw new Error('Utilisateur non connecté');

    // Tentative 1 : endpoint direct sans tontineId
    try {
      const notifications = await firstValueFrom(
        this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}`)
      );
      if (Array.isArray(notifications)) {
        return this.sortByDate(notifications);
      }
    } catch {
      // L'endpoint n'existe pas → on agrège
    }

    // Tentative 2 : agrégation depuis toutes les tontines de l'utilisateur
    let tontines: any[] = [];
    try {
      tontines = await firstValueFrom(
        this.http.get<any[]>(this.tontinesUrl)
      );
    } catch {
      return [];
    }

    if (!tontines || tontines.length === 0) return [];

    const arrays = await Promise.all(
      tontines.map(t =>
        firstValueFrom(
          this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}/tontine/${t.id}`)
        ).catch(() => [] as Notification[])
      )
    );

    const merged = arrays.flat();
    const unique = Array.from(new Map(merged.map(n => [n.id, n])).values());
    return this.sortByDate(unique);
  }

  // Récupérer toutes les notifications d'un utilisateur dans une tontine.
  getNotifications(userId = this.getCurrentUserId(), tontineId = this.getCurrentTontineId()): Observable<Notification[]> {
    if (!this.hasNotificationContext(userId, tontineId)) {
      return this.missingContextError();
    }
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}/tontine/${tontineId}`);
  }

  // Récupérer les notifications non lues d'un utilisateur dans une tontine.
  getUnreadNotifications(userId = this.getCurrentUserId(), tontineId = this.getCurrentTontineId()): Observable<Notification[]> {
    if (!this.hasNotificationContext(userId, tontineId)) {
      return this.missingContextError();
    }
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}/tontine/${tontineId}/non-lues`);
  }

  getUnreadCount(userId = this.getCurrentUserId(), tontineId = this.getCurrentTontineId()): Observable<number> {
    if (!this.hasNotificationContext(userId, tontineId)) {
      return this.missingContextError();
    }
    return this.http.get<number>(`${this.apiUrl}/user/${userId}/tontine/${tontineId}/non-lues/count`);
  }

  // Marquer une notification comme lue.
  markAsRead(notificationId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${notificationId}/lire`, {});
  }

  // Marquer une notification comme non lue.
  markAsUnread(notificationId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${notificationId}/non-lire`, {});
  }

  // Tout marquer comme lu pour un utilisateur dans une tontine.
  markAllAsRead(userId = this.getCurrentUserId(), tontineId = this.getCurrentTontineId()): Observable<void> {
    if (!this.hasNotificationContext(userId, tontineId)) {
      return this.missingContextError();
    }
    return this.http.patch<void>(`${this.apiUrl}/user/${userId}/tontine/${tontineId}/lire-tout`, {});
  }

  // Créer une notification.
  createNotification(notification: NotificationRequest): Observable<Notification> {
    return this.http.post<Notification>(this.apiUrl, notification);
  }

  setCurrentTontineId(tontineId: number | string): void {
    const normalizedTontineId = Number(tontineId);
    if (Number.isFinite(normalizedTontineId)) {
      localStorage.setItem(this.currentTontineStorageKey, normalizedTontineId.toString());
    }
  }

  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }

  private sortByDate(notifications: Notification[]): Notification[] {
    return notifications.sort((a, b) => {
      const toMs = (d: any): number => {
        if (!d) return 0;
        // Tableau Java [year, month, day, hour, min, sec] → mois est 1-based côté Java, 0-based en JS
        if (Array.isArray(d)) return new Date(d[0], d[1] - 1, d[2], d[3] ?? 0, d[4] ?? 0, d[5] ?? 0).getTime();
        return new Date(d).getTime();
      };
      return toMs(b.dateCreation) - toMs(a.dateCreation);
    });
  }

  private getCurrentUserId(): number | null {
    return this.authService.currentUser()?.id ?? this.authService.refreshUser()?.id ?? null;
  }

  private getCurrentTontineId(): number | null {
    const storedTontineId =
      localStorage.getItem(this.currentTontineStorageKey) ||
      localStorage.getItem('tontineId') ||
      localStorage.getItem('idTontine') ||
      sessionStorage.getItem('tontineId') ||
      sessionStorage.getItem('idTontine');
    return storedTontineId ? Number(storedTontineId) : null;
  }

  private hasNotificationContext(userId: number | null, tontineId: number | null): userId is number {
    return Number.isFinite(userId) && Number.isFinite(tontineId);
  }

  private missingContextError<T>(): Observable<T> {
    return throwError(() => new Error('Impossible de charger les notifications sans userId et tontineId.'));
  }
}

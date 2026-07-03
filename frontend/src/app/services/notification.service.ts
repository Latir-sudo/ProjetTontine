// src/app/services/notification.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
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
  private currentTontineStorageKey = 'currentNotificationTontineId';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // Recuperer toutes les notifications d'un utilisateur dans une tontine.
  getNotifications(userId = this.getCurrentUserId(), tontineId = this.getCurrentTontineId()): Observable<Notification[]> {
    if (!this.hasNotificationContext(userId, tontineId)) {
      return this.missingContextError();
    }

    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}/tontine/${tontineId}`);
  }

  // Recuperer les notifications non lues d'un utilisateur dans une tontine.
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

  // Creer une notification.
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

// src/app/services/notification.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications'; // Adaptez à votre URL backend

  constructor(private http: HttpClient) {}

  // Récupérer toutes les notifications du membre connecté
  getNotifications(): Observable<Notification[]> {
    const membreId = this.getCurrentMembreId(); // À implémenter selon votre système d'auth
    return this.http.get<Notification[]>(`${this.apiUrl}/membre/${membreId}`);
  }

  // Récupérer les notifications non lues
  
  getUnreadNotifications(): Observable<Notification[]> {
    const membreId = this.getCurrentMembreId();
    return this.http.get<Notification[]>(`${this.apiUrl}/membre/${membreId}/non-lues`);
  }

  // Compter les notifications non lues
  getUnreadCount(): Observable<number> {
    const membreId = this.getCurrentMembreId();
    return this.http.get<number>(`${this.apiUrl}/membre/${membreId}/non-lues/count`);
  }

  // Marquer une notification comme lue
  markAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/lire`, {});
  }

  // Tout marquer comme lu
  markAllAsRead(): Observable<void> {
    const membreId = this.getCurrentMembreId();
    return this.http.put<void>(`${this.apiUrl}/membre/${membreId}/lire-tout`, {});
  }

  // Supprimer une notification
  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }

  // Récupérer l'ID du membre connecté (à adapter selon votre système d'authentification)
  private getCurrentMembreId(): number {
    // Exemple: récupérer depuis le localStorage ou le token JWT
    const membreId = localStorage.getItem('membreId');
    return membreId ? parseInt(membreId) : 1; // Valeur par défaut pour test
  }
}

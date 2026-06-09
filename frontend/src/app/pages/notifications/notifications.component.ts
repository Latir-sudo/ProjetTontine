// src/app/pages/notifications/notifications.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { Notification } from '../../models/notification.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  loading: boolean = false;
  error: string | null = null;
  private subscriptions: Subscription = new Subscription();

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadNotifications(): void {
    this.loading = true;
    this.error = null;

    const sub = this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des notifications:', err);
        this.error = 'Impossible de charger les notifications. Veuillez réessayer plus tard.';
        this.loading = false;
        // En cas d'erreur, charger les données mockées pour le développement
        this.loadMockData();
      }
    });

    this.subscriptions.add(sub);
  }

  // Données mockées pour le développement (optionnel)
  private loadMockData(): void {
    this.notifications = [
      {
        id: 1,
        titre: 'Paiement validé',
        message: 'Votre cotisation de 5 000 FCFA a été validée',
        tempsRelatif: '2h',
        couleur: '#5bbf7e',
        estLu: false,
        dateCreation: new Date().toISOString(),
        typeNotification: 'PAIEMENT'
      },
      {
        id: 2,
        titre: 'Rappel de cotisation',
        message: 'Votre cotisation arrive à échéance dans 3 jours',
        tempsRelatif: '5h',
        couleur: '#ffc86a',
        estLu: false,
        dateCreation: new Date().toISOString(),
        typeNotification: 'RAPPEL'
      },
      {
        id: 3,
        titre: 'Nouveau membre',
        message: 'Khadidja Sow a rejoint votre tontine',
        tempsRelatif: '1j',
        couleur: '#97b3ff',
        estLu: false,
        dateCreation: new Date().toISOString(),
        typeNotification: 'MEMBRE'
      },
      {
        id: 4,
        titre: 'Cotisation en retard',
        message: 'Votre cotisation de Mai est en retard',
        tempsRelatif: '1j',
        couleur: '#ff7262',
        estLu: false,
        dateCreation: new Date().toISOString(),
        typeNotification: 'RETARD'
      }
    ];
  }

  markAllRead(): void {
    const sub = this.notificationService.markAllAsRead().subscribe({
      next: () => {
        // Mettre à jour localement
        this.notifications = this.notifications.map((notification) => ({
          ...notification,
          estLu: true
        }));
      },
      error: (err) => {
        console.error('Erreur lors du marquage:', err);
        // Fallback: mise à jour locale même en cas d'erreur
        this.notifications = this.notifications.map((notification) => ({
          ...notification,
          estLu: true
        }));
      }
    });

    this.subscriptions.add(sub);
  }

  markSingleRead(notificationId: number): void {
    const sub = this.notificationService.markAsRead(notificationId).subscribe({
      next: () => {
        const notification = this.notifications.find(n => n.id === notificationId);
        if (notification) {
          notification.estLu = true;
        }
      },
      error: (err) => {
        console.error('Erreur lors du marquage:', err);
        // Fallback: mise à jour locale
        const notification = this.notifications.find(n => n.id === notificationId);
        if (notification) {
          notification.estLu = true;
        }
      }
    });

    this.subscriptions.add(sub);
  }

  deleteNotification(notificationId: number): void {
    if (confirm('Voulez-vous supprimer cette notification ?')) {
      const sub = this.notificationService.deleteNotification(notificationId).subscribe({
        next: () => {
          this.notifications = this.notifications.filter(n => n.id !== notificationId);
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          // Fallback: suppression locale
          this.notifications = this.notifications.filter(n => n.id !== notificationId);
        }
      });

      this.subscriptions.add(sub);
    }
  }

  trackByNotificationId(index: number, notification: Notification): number {
    return notification.id;
  }
}

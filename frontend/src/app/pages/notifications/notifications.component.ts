// src/app/pages/notifications/notifications.component.ts
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
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

  constructor(
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadNotifications(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();
    const sub = this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des notifications:', err);
        this.error = 'Impossible de charger les notifications. Veuillez réessayer plus tard.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });

    this.subscriptions.add(sub);
  }


  markAllRead(): void {
    const sub = this.notificationService.markAllAsRead().subscribe({
      next: () => {
        // Mettre à jour localement
        this.notifications = this.notifications.map((notification) => ({
          ...notification,
          estLu: true
        }));
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du marquage:', err);
        // Fallback: mise à jour locale même en cas d'erreur
        this.notifications = this.notifications.map((notification) => ({
          ...notification,
          estLu: true
        }));
        this.cdr.detectChanges();
      }
    });

    this.subscriptions.add(sub);
  }

  markSingleRead(notificationId: number): void {
    const sub = this.notificationService.markAsRead(notificationId).subscribe({
      next: () => {
        this.notifications = this.notifications.map(notification =>
          notification.id === notificationId ? { ...notification, estLu: true } : notification
        );
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du marquage:', err);
        // Fallback: mise à jour locale
        this.notifications = this.notifications.map(notification =>
          notification.id === notificationId ? { ...notification, estLu: true } : notification
        );
        this.cdr.detectChanges();
      }
    });

    this.subscriptions.add(sub);
  }

  deleteNotification(notificationId: number): void {
    if (confirm('Voulez-vous supprimer cette notification ?')) {
      const sub = this.notificationService.deleteNotification(notificationId).subscribe({
        next: () => {
          this.notifications = this.notifications.filter(n => n.id !== notificationId);
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          // Fallback: suppression locale
          this.notifications = this.notifications.filter(n => n.id !== notificationId);
          this.cdr.detectChanges();
        }
      });

      this.subscriptions.add(sub);
    }
  }

  trackByNotificationId(index: number, notification: Notification): number {
    return notification.id;
  }
}

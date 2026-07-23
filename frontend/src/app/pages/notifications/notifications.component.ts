// src/app/pages/notifications/notifications.component.ts
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { Notification } from '../../models/notification.model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  loading = false;
  error: string | null = null;

  // Filtre actif : 'all' | 'unread' | 'ADHESION' | 'PAIEMENT' | 'INFO'
  activeFilter: string = 'all';

  private pollingInterval: any;

  constructor(
    private notificationService: NotificationService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
    // Rafraîchissement automatique toutes les 30s
    this.pollingInterval = setInterval(() => this.loadNotifications(false), 30000);
  }

  ngOnDestroy(): void {
    if (this.pollingInterval) clearInterval(this.pollingInterval);
  }

  async loadNotifications(showLoader = true): Promise<void> {
    if (showLoader) {
      this.loading = true;
      this.error = null;
      this.cdr.detectChanges();
    }

    try {
      this.notifications = await this.notificationService.getAllUserNotifications();
      this.cdr.detectChanges();
    } catch (err: any) {
      console.error('Erreur chargement notifications:', err);
      this.error = 'Impossible de charger les notifications. Vérifiez votre connexion.';
      this.cdr.detectChanges();
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  get filteredNotifications(): Notification[] {
    let list = this.notifications;
    if (this.activeFilter === 'unread') {
      list = list.filter(n => !n.estLu);
    } else if (this.activeFilter !== 'all') {
      list = list.filter(n => n.typeNotification === this.activeFilter);
    }
    return list;
  }

  get unreadCount(): number {
    return this.notifications.filter(n => !n.estLu).length;
  }

  get adhesionCount(): number {
    return this.notifications.filter(n => this.isAdhesionType(n) && !n.estLu).length;
  }

  setFilter(filter: string): void {
    this.activeFilter = filter;
    this.cdr.detectChanges();
  }

  /** Navigation intelligente selon le type de notification */
  async handleNotificationClick(notification: Notification): Promise<void> {
    // Marquer comme lu
    if (!notification.estLu) {
      this.markSingleRead(notification.id);
    }

    // Si c'est une demande d'adhésion et qu'on a un tontineId → page de détail avec modal
    if (this.isAdhesionType(notification) && notification.idTontine) {
      this.router.navigate(['/tontine', notification.idTontine], {
        queryParams: { showDemandes: 'true' }
      });
      return;
    }

    // Si lienAction est renseigné par le back
    if (notification.lienAction) {
      this.router.navigateByUrl(notification.lienAction);
      return;
    }

    // Si la notification a un idTontine → page de détail
    if (notification.idTontine) {
      this.router.navigate(['/tontine', notification.idTontine]);
    }
  }

  isAdhesionType(notification: Notification): boolean {
    const type = (notification.typeNotification || '').toUpperCase();
    return type.includes('ADHESION') || type.includes('DEMANDE');
  }

  isPaiementType(notification: Notification): boolean {
    const type = (notification.typeNotification || '').toUpperCase();
    return type.includes('PAIEMENT') || type.includes('COTISATION');
  }

  getTypeBadge(notification: Notification): { label: string; css: string } {
    if (this.isAdhesionType(notification)) {
      return { label: 'Adhésion', css: 'badge-adhesion' };
    }
    if (this.isPaiementType(notification)) {
      return { label: 'Paiement', css: 'badge-paiement' };
    }
    const type = notification.typeNotification || 'INFO';
    return { label: type.charAt(0) + type.slice(1).toLowerCase(), css: 'badge-info' };
  }

  getNotificationIcon(notification: Notification): string {
    if (this.isAdhesionType(notification)) return 'ri-user-add-line';
    if (this.isPaiementType(notification)) return 'ri-money-dollar-circle-line';
    return 'ri-information-line';
  }

  async markAllRead(): Promise<void> {
    this.notifications = this.notifications.map(n => ({ ...n, estLu: true }));
    this.cdr.detectChanges();

    try {
      // Marquer tontine par tontine si nécessaire
      const tontineIds = [...new Set(this.notifications.map(n => n.idTontine).filter(Boolean))];
      await Promise.all(
        tontineIds.map(id =>
          firstValueFrom(
            this.notificationService.markAllAsRead(undefined, id as number)
          ).catch(() => null)
        )
      );
    } catch {
      // Mise à jour locale déjà effectuée
    }
  }

  markSingleRead(notificationId: number): void {
    this.notifications = this.notifications.map(n =>
      n.id === notificationId ? { ...n, estLu: true } : n
    );
    this.cdr.detectChanges();
    this.notificationService.markAsRead(notificationId).subscribe({ error: () => null });
  }

  deleteNotification(event: Event, notificationId: number): void {
    event.stopPropagation();
    this.notifications = this.notifications.filter(n => n.id !== notificationId);
    this.cdr.detectChanges();
    this.notificationService.deleteNotification(notificationId).subscribe({ error: () => null });
  }

  trackByNotificationId(_: number, notification: Notification): number {
    return notification.id;
  }

  /** Convertit dateCreation (ISO string ou tableau Java) en Date JS */
  toDate(raw: string | number[]): Date {
    if (Array.isArray(raw)) {
      return new Date(raw[0], raw[1] - 1, raw[2], raw[3] ?? 0, raw[4] ?? 0, raw[5] ?? 0);
    }
    return new Date(raw);
  }
}

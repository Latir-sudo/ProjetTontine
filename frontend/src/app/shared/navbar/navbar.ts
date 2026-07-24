import { Component, OnInit, OnDestroy, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../services/auth.services';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class Navbar implements OnInit, OnDestroy {
  mobileMenuOpen = false;
  currentYear = new Date().getFullYear();
  unreadCount = 0;
  private pollingInterval: any;

  constructor(
    public authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {
    effect(() => {
      const loggedIn = this.authService.isLoggedIn();
      if (loggedIn) {
        this.startPolling();
      } else {
        this.stopPolling();
        this.unreadCount = 0;
      }
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.stopPolling();
  }

  private startPolling(): void {
    if (this.pollingInterval) return;
    this.loadUnreadCount();
    this.pollingInterval = setInterval(() => this.loadUnreadCount(), 30000);
  }

  private stopPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  async loadUnreadCount(): Promise<void> {
    if (!this.authService.isLoggedInNow()) return;
    try {
      const notifications = await this.notificationService.getAllUserNotifications();
      this.unreadCount = notifications.filter(n => !n.estLu).length;
    } catch {
      this.unreadCount = 0;
    }
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedInNow();
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/accueil']);
    this.mobileMenuOpen = false;
  }

  toggleMobileMenu() {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }
}
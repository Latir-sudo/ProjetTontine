import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.services';

@Injectable({ providedIn: 'root' })
export class AuthGuard {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const token = this.authService.getToken();
    // Vérifie la présence d'un token et qu'il n'est pas expiré
    if (token && !this.authService.isTokenExpired(token)) {
      // Recharge l'utilisateur si besoin
      if (!this.authService.currentUser() || !this.authService.isLoggedIn()) {
        this.authService.refreshUser();
      }
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
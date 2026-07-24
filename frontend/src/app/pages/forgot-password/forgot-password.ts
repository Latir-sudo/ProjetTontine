import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.scss']
})
export class ForgotPassword {
  private apiUrl = 'http://localhost:8080/api/auth';

  currentStep = 1;
  email = '';
  code = '';
  newPassword = '';
  confirmPassword = '';

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  submitEmail() {
    if (!this.email.trim()) {
      this.errorMessage = 'Veuillez entrer votre adresse email';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post<any>(`${this.apiUrl}/forgot-password`, { email: this.email.trim() })
      .subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.successMessage = 'Code envoyé ! Vérifiez votre boîte mail (et vos spams).';
            this.currentStep = 2;
          } else {
            this.errorMessage = response.message || 'Aucun compte trouvé avec cet email';
          }
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = 'Impossible de contacter le serveur. Vérifiez que le backend est démarré.';
          this.cdr.detectChanges();
        }
      });
  }

  verifyCode() {
    this.code = this.code.trim();
    if (!this.code || this.code.length !== 6) {
      this.errorMessage = 'Veuillez entrer le code à 6 chiffres';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post<any>(`${this.apiUrl}/verify-reset-code`, {
      email: this.email.trim(),
      code: this.code
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.successMessage = 'Code vérifié !';
          this.currentStep = 3;
        } else {
          this.errorMessage = response.message || 'Code invalide ou expiré';
        }
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Impossible de contacter le serveur.';
        this.cdr.detectChanges();
      }
    });
  }

  resetPassword() {
    if (!this.newPassword || this.newPassword.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post<any>(`${this.apiUrl}/reset-password`, {
      email: this.email.trim(),
      code: this.code.trim(),
      newPassword: this.newPassword
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.successMessage = 'Mot de passe réinitialisé avec succès !';
          setTimeout(() => this.router.navigate(['/login']), 2000);
        } else {
          this.errorMessage = response.message || 'Erreur lors de la réinitialisation';
        }
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Impossible de contacter le serveur.';
        this.cdr.detectChanges();
      }
    });
  }
}

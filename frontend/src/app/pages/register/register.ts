import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.services';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './register.html',
  styleUrls: ['./register.scss']
})
export class Register {
  // Structure adaptée au backend Spring Boot
  userData = {
    nom: '',           // correspond à UserRequest.nom
    prenom: '',        // correspond à UserRequest.prenom
    email: '',         // correspond à UserRequest.email
    telephone: '',     // correspond à UserRequest.telephone
    password: '',      // correspond à UserRequest.password
    ville: 'DAKAR',    // correspond à UserRequest.ville
    roles: ['USER']    // rôle par défaut
  };
  
  confirmPassword: string = ''; // pour la confirmation du mot de passe
  // Liste des localités pour le select
  localites = ['DAKAR', 'THIES', 'SAINT-LOUIS', 'TOUBA', 'ZIGUINCHOR', 'KAOLACK', 'TAMBACOUNDA'];
  
  // États du formulaire
  errorMessage = '';
  successMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  async register() {
    // Réinitialisation des messages
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Validation simple
    if (!this.isFormValid()) {
      this.errorMessage = 'Veuillez remplir tous les champs correctement';
      this.isLoading = false;
      return;
    }

    try {
      const result = await this.authService.register(this.userData);

      if (result.success) {
        this.successMessage = result.message + ' Redirection en cours...';
        // Redirection vers le dashboard après 2 secondes
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 2000);
      } else {
        this.errorMessage = result.message;
      }
    } catch (error) {
      console.error('Erreur inscription:', error);
      this.errorMessage = 'Erreur de connexion au serveur. Veuillez réessayer.';
    } finally {
      this.isLoading = false;
    }
  }

  private isFormValid(): boolean {
    return this.userData.nom.trim() !== '' &&
           this.userData.prenom.trim() !== '' &&
           this.userData.email.trim() !== '' &&
           this.userData.telephone.trim() !== '' &&
           this.userData.password.trim().length >= 8 &&
           
           this.userData.password === this.confirmPassword;
  }
}
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.services';

@Component({
  selector: 'app-tontine-create',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './create-tontine.html',
  styleUrls: ['./create-tontine.scss']
})
export class TontineCreate {
  
  // Formulaire - correspond exactement au DTO TontineRequest
  form = {
    nomTontine: '',
    frequence: 'MENSUEL',
    montant: 0,
    dateCreation: new Date().toISOString().split('T')[0], // Format YYYY-MM-DD
    descriptionTontine: '',
    politiqueTontine: 'Standard',
    categorie: '',
    region: 'Dakar',
    nombreMax: 10
  };
  
  // Options pour les sélecteurs
  categories = ['Épargne', 'Investissement', 'Sociale', 'Commerce', 'Famille', 'Solidaire'];
  frequencies = ['MENSUEL', 'HEBDOMADAIRE', 'BIMENSUEL', 'TRIMESTRIEL'];
  localites = ['Dakar', 'Thiès', 'Saint-Louis', 'Kaolack', 'Touba', 'Ziguinchor'];
  
  // États
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  async create() {
    // Réinitialisation des messages
    this.errorMessage = '';
    this.successMessage = '';
    
    // Validation
    if (!this.form.nomTontine.trim()) {
      this.errorMessage = 'Le nom de la tontine est requis';
      return;
    }
    
    if (this.form.montant < 1000) {
      this.errorMessage = 'Le montant doit être au minimum de 1000 FCFA';
      return;
    }
    
    if (!this.form.categorie) {
      this.errorMessage = 'Veuillez sélectionner une catégorie';
      return;
    }
    
    if (this.form.nombreMax < 2) {
      this.errorMessage = 'Le nombre maximum de membres doit être au moins 2';
      return;
    }
    
    this.isLoading = true;
    
    try {
      // Données exactement comme attendu par le backend TontineRequest
      const tontineData = {
        nomTontine: this.form.nomTontine,
        frequence: this.form.frequence,
        montant: this.form.montant,
        dateCreation: this.form.dateCreation,
        descriptionTontine: this.form.descriptionTontine || null,
        politiqueTontine: this.form.politiqueTontine,
        categorie: this.form.categorie,
        region: this.form.region,
        nombreMax: this.form.nombreMax
      };
      
      console.log('📤 Envoi au backend:', tontineData);
      
      // Appel API POST /api/tontine
      const response = await this.apiService.post('/tontine', tontineData);
      
      console.log('✅ Réponse:', response);
      
      this.successMessage = 'Tontine créée avec succès ! Redirection...';
      
      // Redirection vers le dashboard après 2 secondes
      setTimeout(() => {
        this.router.navigate(['/dashboard']);
      }, 2000);
      
    } catch (error: any) {
      console.error('❌ Erreur création tontine:', error);
      this.errorMessage = error.error?.message || 'Erreur lors de la création de la tontine';
    } finally {
      this.isLoading = false;
    }
  }
}
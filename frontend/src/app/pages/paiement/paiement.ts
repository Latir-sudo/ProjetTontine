import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.services';
import { NotificationService } from '../../services/notification.service';
import { PaiementService } from '../../services/paiement.service';

interface Tontine {
  id: number;
  nomTontine: string;
  montant: number;
  idAdmin?: number;
  prenomAdmin?: string;
  nomAdmin?: string;
  telephoneAdmin?: string;
  region?: string;
}

@Component({
  selector: 'app-paiement',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './paiement.html',
  styleUrls: ['./paiement.scss']
})
export class Paiement implements OnInit {
  tontine: Tontine | null = null;
  selectedMethod: 'WAVE' = 'WAVE';
  phoneNumber = '';
  isLoading = false;
  errorMessage = '';
  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';
  private hasLoaded = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private apiService: ApiService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private paiementService: PaiementService
  ) {}

  async ngOnInit() {
    const tontineId = this.route.snapshot.paramMap.get('id');

    if (!tontineId) {
      this.errorMessage = 'Tontine non spécifiée';
      this.isLoading = false;
      return;
    }

    const parsedTontineId = parseInt(tontineId, 10);
    this.notificationService.setCurrentTontineId(parsedTontineId);

    if (!this.hasLoaded) {
      this.hasLoaded = true;
      await this.loadTontineData(parsedTontineId);
    }
  }

  selectPaymentMethod(method: 'WAVE') {
    this.selectedMethod = method;
    this.feedbackMessage = '';
    this.errorMessage = '';
  }

  async payer() {
    if (!this.tontine?.id) {
      this.errorMessage = 'Aucune tontine sélectionnée';
      return;
    }

    const phone = this.phoneNumber.trim();
    if (!phone) {
      this.errorMessage = 'Veuillez saisir un numéro de téléphone';
      return;
    }

    const currentUser = this.authService.currentUser();
    if (!currentUser?.id) {
      this.errorMessage = 'Vous devez être connecté pour effectuer un paiement';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.feedbackMessage = '';

    try {
      const cotisationId = await this.getOrCreateCotisation(currentUser.id, this.tontine.id);
      const mobileResponse = await this.paiementService.createWaveCheckout(cotisationId, phone).toPromise();

      if (!mobileResponse?.success) {
        throw new Error(mobileResponse?.message || 'Échec de l’initiation du paiement mobile');
      }

      if (!mobileResponse?.launchUrl) {
        throw new Error('Wave n’a pas renvoyé de lien de paiement.');
      }
      localStorage.setItem('wave_checkout_id', mobileResponse.transactionId);
      window.location.assign(mobileResponse.launchUrl);
      return;
    } catch (error: any) {
      console.error('Erreur paiement :', error);
      this.feedbackType = 'error';
      this.feedbackMessage = error?.error?.message || error?.message || 'Le paiement n’a pas pu être enregistré';
    } finally {
      this.isLoading = false;
    }
  }

  private async loadTontineData(id: number) {
    this.isLoading = true;
    this.errorMessage = '';
    this.feedbackMessage = '';

    try {
      const cached = localStorage.getItem(`tontine_${id}`);
      if (cached) {
        try {
          this.tontine = JSON.parse(cached);
          console.log('Données chargées depuis le cache');
        } catch (e) {
          console.error('Erreur parsing cache :', e);
        }
      }

      const freshData = await this.apiService.get<Tontine>(`/tontine/${id}`);
      if (freshData) {
        this.tontine = freshData;
        localStorage.setItem(`tontine_${id}`, JSON.stringify(freshData));
        console.log('Données rafraichies depuis l\'API');
      }
    } catch (error) {
      console.error('Erreur API :', error);
      if (!this.tontine) {
        this.errorMessage = 'Impossible de charger les informations de la tontine';
      }
    } finally {
      this.isLoading = false;
    }
  }

  private async getOrCreateCotisation(userId: number, tontineId: number): Promise<number> {
    const cotisations = await this.apiService.get<any[]>(`/cotisations/user/${userId}/tontine/${tontineId}`);

    if (Array.isArray(cotisations) && cotisations.length > 0 && cotisations[0]?.id) {
      return cotisations[0].id;
    }

    const createdCotisation = await this.apiService.post<any>(`/cotisations/user/${userId}/tontine/${tontineId}`, {
      montant: this.tontine?.montant || 0
    });

    return createdCotisation?.id;
  }
}

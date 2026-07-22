import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.services';
import { NotificationService } from '../../services/notification.service';

interface Tontine {
  id: number;
  nomTontine: string;
  descriptionTontine?: string;
  montant: number;
  frequence?: string;
  region?: string;
  categorie?: string;
  nombreMembres?: number;
  nombreMax?: number;
  statutTontine?: string;
  dateCreation?: string;
  idAdmin?: number;
  prenomAdmin?: string;
  nomAdmin?: string;
  telephoneAdmin?: string;
}

interface DemandeAdhesion {
  idUser: number;
  idTontine?: number;
  prenomUser: string;
  nomUser: string;
  telephoneUser: string;
  emailUser: string;
  dateAdhesion: string;
  statut: 'ATTENTE' | 'ACCEPTEE' | 'REJETEE';
}

@Component({
  selector: 'app-tontine-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './detail-tontine.html',
  styleUrls: ['./detail-tontine.scss']
})
export class DetailTontine implements OnInit {
  
  tontine: Tontine | null = null;
  isLoading = true;
  errorMessage = '';
  isJoined = false;
  isAdmin = false;
  demandes: DemandeAdhesion[] = [];
  showDemandesModal = false;
  imageError = false;
  feedbackMessage = '';
  feedbackType: 'success' | 'info' | 'error' = 'info';

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const tontineId = parseInt(id, 10);
      this.notificationService.setCurrentTontineId(tontineId);
      await this.loadTontineDetail(tontineId);
      await this.checkIfJoined();
      await this.checkIfAdmin();
      if (this.isAdmin) {
        await this.loadDemandesAdhesion();
      }
      this.cdr.detectChanges();
    } else {
      this.errorMessage = 'ID de tontine non trouvé';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private async loadTontineDetail(id: number) {
    this.isLoading = true;
    this.cdr.detectChanges();
    try {
      this.tontine = await this.apiService.get<Tontine>(`/tontine/${id}`);
      console.log('Détails tontine chargés:', this.tontine);
    } catch (error: any) {
      console.error('Erreur chargement détail:', error);
      this.errorMessage = 'Impossible de charger les détails de la tontine';
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private async checkIfJoined() {
    if (!this.tontine) return;
    try {
      const mesTontines = await this.apiService.get<Tontine[]>('/tontine/mes-tontines');
      this.isJoined = mesTontines.some(t => t.id === this.tontine?.id);
      console.log('Est membre ?', this.isJoined);
    } catch (error: any) {
      console.error('Erreur vérification adhésion:', error);
      this.isJoined = false;
    }
  }

  async join() {
    if (!this.tontine) return;
    
    try {
      const currentUser = await this.authService.currentUser();
      
      if (!currentUser || !currentUser.id) {
        this.feedbackMessage = '❌ Vous devez être connecté pour adhérer à une tontine';
        this.feedbackType = 'error';
        return;
      }

      const existingRequests = await this.apiService.get<any[]>(`/tontine/${this.tontine.id}/adhesion`);
      const alreadyRequested = existingRequests?.some((request: any) => request.idUser === currentUser.id);

      if (alreadyRequested) {
        this.feedbackMessage = 'ℹ️ Une demande d’adhésion a déjà été envoyée pour cette tontine.';
        this.feedbackType = 'info';
        return;
      }
      
      const adhesionRequest = {
        idUser: currentUser.id,
        dateAdhesion: new Date().toISOString().split('T')[0]
      };
      
      console.log('Envoi de la demande:', adhesionRequest);
      await this.apiService.post(`/tontine/${this.tontine.id}/adhesion`, adhesionRequest);
      this.feedbackMessage = '✅ Demande d’adhésion envoyée avec succès !';
      this.feedbackType = 'success';
      await this.checkIfJoined();
      this.cdr.detectChanges();
    } catch (error: any) {
      console.error('Erreur adhésion:', error);
      this.feedbackMessage = '❌ Erreur lors de la demande d’adhésion';
      this.feedbackType = 'error';
    }
  }

  private async checkIfAdmin() {
    if (!this.tontine) return;
    try {
      const currentUser = await this.authService.currentUser();
      if (currentUser && this.tontine?.idAdmin) {
        this.isAdmin = currentUser.id === this.tontine.idAdmin;
        console.log('Est admin ?', this.isAdmin);
      }
    } catch (error: any) {
      console.error('Erreur vérification admin:', error);
      this.isAdmin = false;
    }
  }

  private async loadDemandesAdhesion() {
    if (!this.tontine || !this.isAdmin) return;

    try {
      const demandes = await this.apiService.get<any[]>(`/tontine/${this.tontine.id}/adhesion`);
      console.log('Demandes reçues:', demandes);
      this.demandes = demandes.filter(d => d.statut === 'ATTENTE');
      console.log('Demandes en attente:', this.demandes);
    } catch (error: any) {
      console.error('Erreur chargement demandes:', error);
      this.demandes = [];
    }
  }

  async gererDemandes() {
    if (!this.tontine) return;
    await this.loadDemandesAdhesion();
    this.showDemandesModal = true;
    this.cdr.detectChanges();
  }

  fermerModal() {
    this.showDemandesModal = false;
    this.cdr.detectChanges();
  }

  async approuverDemande(demande: DemandeAdhesion) {
    if (!this.tontine) return;
    
    try {
      console.log('Approbation de la demande:', demande);
      await this.apiService.patch(`/tontine/${this.tontine.id}/adhesion?idUser=${demande.idUser}`, {
        statut: 'ACCEPTEE'
      });
      alert(`✅ ${demande.prenomUser} ${demande.nomUser} est maintenant membre de la tontine !`);
      await this.loadDemandesAdhesion();
      await this.loadTontineDetail(this.tontine.id);
      if (this.demandes.length === 0) {
        this.fermerModal();
      }
      this.cdr.detectChanges();
    } catch (error: any) {
      console.error('Erreur approbation:', error);
      alert('❌ Erreur lors de l\'approbation');
    }
  }

  async rejeterDemande(demande: DemandeAdhesion) {
    if (!this.tontine) return;
    
    if (!confirm(`Êtes-vous sûr de vouloir rejeter la demande de ${demande.prenomUser} ${demande.nomUser} ?`)) {
      return;
    }
    
    try {
      await this.apiService.patch(`/tontine/${this.tontine.id}/adhesion?idUser=${demande.idUser}`, {
        statut: 'REJETEE'
      });
      alert(`❌ Demande de ${demande.prenomUser} ${demande.nomUser} rejetée.`);
      await this.loadDemandesAdhesion();
      if (this.demandes.length === 0) {
        this.fermerModal();
      }
      this.cdr.detectChanges();
    } catch (error: any) {
      console.error('Erreur rejet:', error);
      alert('❌ Erreur lors du rejet');
    }
  }

  onImageError() {
    this.imageError = true;
    this.cdr.detectChanges();
  }

  retryImage() {
    this.imageError = false;
    this.cdr.detectChanges();
  }

  getProgressPercentage(): number {
    if (!this.tontine || !this.tontine.nombreMembres) return 0;
    const max = this.tontine.nombreMax || 20;
    const percentage = (this.tontine.nombreMembres / max) * 100;
    return Math.min(percentage, 100);
  }

  goToPayment(): void {
    if (!this.tontine) return;
    this.router.navigate(['/paiement', this.tontine.id]);
  }

  gererMembres(): void {
    if (!this.tontine) return;
    this.router.navigate(['/tontine/gestion-membres', this.tontine.id]);
  }
}

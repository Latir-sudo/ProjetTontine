import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

interface Tontine {
  id: number;
  nomTontine: string;
  montant: number;
  nombreMembres?: number;
  nombreMax?: number;
  admin?: {
    id: number;
    nom: string;
    prenom: string;
  };
}

interface DemandeAdhesion {
  idUser: number;
  idTontine: number;
  prenomUser: string;
  nomUser: string;
  telephoneUser: string;
  emailUser?: string;
  dateAdhesion: string;
  statut: 'ATTENTE' | 'ACCEPTEE' | 'REJETEE';
}

interface PaiementHistoriqueResponse {
  id: number;
  montant: number;
  datePaiement: string;
  modePaiement: 'ORANGE_MONEY' | 'WAVE' | 'FREE_MONEY';
  reference: string;
  valide: boolean;
  titreCotisation: string;
  cotisationId: number;
  membreNom: string;
  membrePrenom: string;
}

interface PaiementResponse {
  id: number;
  montant: number;
  datePaiement: string;
  modePaiement: 'ORANGE_MONEY' | 'WAVE' | 'FREE_MONEY';
  reference: string;
  valide: boolean;
  cotisationId: number;
}

@Component({
  selector: 'app-administration',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './administration.html',
  styleUrls: ['./administration.scss']
})
export class Administration implements OnInit {

  tontine: Tontine | null = null;
  requests: DemandeAdhesion[] = [];
  payments: PaiementResponse[] = [];
  paiementsHistorique: PaiementHistoriqueResponse[] = [];

  isLoading = true;
  errorMessage = '';

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      await this.loadData(parseInt(id, 10));
    } else {
      this.errorMessage = 'ID de tontine non trouve';
      this.isLoading = false;
    }
  }

  private async loadData(tontineId: number): Promise<void> {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    try {
      this.tontine = await this.apiService.get<Tontine>(`/tontine/${tontineId}`);

      const allRequests = await this.apiService.get<DemandeAdhesion[]>(`/tontine/${tontineId}/adhesion`);
      this.requests = allRequests.filter(r => r.statut === 'ATTENTE');

      this.paiementsHistorique = await this.apiService.get<PaiementHistoriqueResponse[]>(`/paiements/tontine/${tontineId}/historique`);
      this.payments = this.paiementsHistorique
        .filter(p => p.valide === false)
        .map(p => ({
          id: p.id,
          montant: p.montant,
          datePaiement: p.datePaiement,
          modePaiement: p.modePaiement,
          reference: p.reference,
          valide: p.valide,
          cotisationId: p.cotisationId
        }));
    } catch (error) {
      console.error('Erreur chargement:', error);
      this.errorMessage = 'Impossible de charger les donnees. Verifiez votre connexion.';
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  async acceptRequest(index: number): Promise<void> {
    const request = this.requests[index];
    if (!request || request.statut !== 'ATTENTE' || !this.tontine) return;

    try {
      await this.apiService.patch(`/tontine/${this.tontine.id}/adhesion?idUser=${request.idUser}`, {
        statut: 'ACCEPTEE'
      });
      alert(`Demande de ${request.prenomUser} ${request.nomUser} approuvee !`);

      await this.loadData(this.tontine.id);
    } catch (error) {
      console.error('Erreur approbation:', error);
      alert('Erreur lors de l\'approbation');
    }
  }

  async rejectRequest(index: number): Promise<void> {
    const request = this.requests[index];
    if (!request || request.statut !== 'ATTENTE' || !this.tontine) return;

    try {
      await this.apiService.patch(`/tontine/${this.tontine.id}/adhesion?idUser=${request.idUser}`, {
        statut: 'REJETEE'
      });
      alert(`Demande de ${request.prenomUser} ${request.nomUser} rejetee`);

      await this.loadData(this.tontine.id);
    } catch (error) {
      console.error('Erreur rejet:', error);
      alert('Erreur lors du rejet');
    }
  }

  async validerPaiement(index: number): Promise<void> {
    const payment = this.payments[index];
    if (!payment) return;

    try {
      await this.apiService.put(`/paiements/${payment.id}/valider`, {});

      if (this.tontine) {
        await this.loadData(this.tontine.id);
      }
    } catch (error) {
      console.error('Erreur validation paiement:', error);
      alert('Erreur lors de la validation du paiement');
    }
  }

  async rejeterPaiement(index: number): Promise<void> {
    const payment = this.payments[index];
    if (!payment) return;

    try {
      await this.apiService.put(`/paiements/${payment.id}/rejeter`, {});

      if (this.tontine) {
        await this.loadData(this.tontine.id);
      }
    } catch (error) {
      console.error('Erreur rejet paiement:', error);
      alert('Erreur lors du rejet du paiement');
    }
  }

  getStatusLabel(payment: PaiementResponse): string {
    if (payment.valide === true) return 'Confirme';
    if (payment.valide === false) return 'Rejete';
    return 'En attente';
  }

  getStatusClass(payment: PaiementResponse): string {
    if (payment.valide === true) return 'confirmed';
    if (payment.valide === false) return 'rejected';
    return 'pending';
  }

  getRequestLabel(status: string): string {
    const labels: Record<string, string> = {
      ATTENTE: 'En attente',
      ACCEPTEE: 'Approuve',
      REJETEE: 'Rejete'
    };
    return labels[status] || status;
  }

  getMethodLabel(method: string): string {
    const labels: Record<string, string> = {
      ORANGE_MONEY: 'Orange Money',
      WAVE: 'Wave',
      FREE_MONEY: 'Free Money'
    };
    return labels[method] || method;
  }

  get stats() {
    return {
      membresActifs: this.tontine?.nombreMembres || 0,
      demandesEnAttente: this.requests.length,
      paiementsAValider: this.payments.length,
      transactionsConfirmees: this.paiementsHistorique.filter(p => p.valide === true).length
    };
  }

  goBack(): void {
    if (this.tontine) {
      this.router.navigate(['/tontine', this.tontine.id]);
    } else {
      this.router.navigate(['/dashboard']);
    }
  }
}

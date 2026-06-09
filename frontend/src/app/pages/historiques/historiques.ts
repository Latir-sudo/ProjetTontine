// pages/historiques/historiques.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaiementService } from '../../services/paiement.service';
import { PaiementHistorique, PaiementStats } from '../../models/paiement.model';

@Component({
  selector: 'app-historiques',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './historiques.html',
  styleUrls: ['./historiques.scss']
})
export class Historiques implements OnInit {

  filter: 'all' | 'success' | 'pending' | 'failed' = 'all';
  payments: PaiementHistorique[] = [];
  stats: PaiementStats | null = null;
  isLoading: boolean = true;
  errorMessage: string | null = null;

  currentMembreId: number = 1;

  constructor(
    private paiementService: PaiementService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadHistorique();
    this.loadStats();
    }

  loadHistorique(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.cdr.detectChanges(); 

    this.paiementService.getHistoriqueByMembre(this.currentMembreId).subscribe({
      next: (data) => {
        console.log('Historique reçu:', data);
        this.payments = data;
        this.isLoading = false;
        this.cdr.detectChanges(); // ✅ Force la mise à jour de l'affichage
      },
      error: (err) => {
        console.error('Erreur chargement historique:', err);
        this.errorMessage = err.error?.message || 'Impossible de charger votre historique.';
        this.isLoading = false;
        this.cdr.detectChanges(); // ✅ Force l'affichage de l'erreur
      }
    });
  }

  loadStats(): void {
    this.paiementService.getStatsByMembre(this.currentMembreId).subscribe({
      next: (data) => {
        console.log('Stats reçues:', data);
        this.stats = data;
        this.cdr.detectChanges(); // ✅ Force la mise à jour des stats
      },
      error: (err) => {
        console.error('Erreur chargement stats:', err);
        // Optionnel : afficher une erreur pour les stats
      }
    });
  }

  setFilter(filter: 'all' | 'success' | 'pending' | 'failed'): void {
    this.filter = filter;
  }

  get filteredPayments(): PaiementHistorique[] {
    switch (this.filter) {
      case 'success':
        return this.payments.filter(p => p.valide === true);
      case 'pending':
        return this.payments.filter(p => p.valide === false);
      case 'failed':
        return this.payments.filter(p => p.valide === null);
      default:
        return this.payments;
    }
  }

  getStatusLabel(payment: PaiementHistorique): string {
    if (payment.valide === true) return 'Réussi';
    if (payment.valide === false) return 'Attente';
    return 'false';
  }

  getStatusClass(payment: PaiementHistorique): string {
    if (payment.valide === true) return 'success';
    if (payment.valide === false) return 'failed';
    return 'pending';
  }

  getMethodLabel(method: string): string {
    const labels: Record<string, string> = {
      'ORANGE_MONEY': 'Orange Money',
      'WAVE': 'Wave',
      'FREE_MONEY': 'Free Money'
    };
    return labels[method] || method;
  }

  getFilterLabel(): string {
    const labels: Record<string, string> = {
      'all': '',
      'success': 'réussie',
      'pending': 'en attente',
      'failed': 'échouée'
    };
    return labels[this.filter];
  }

  getProgressPercentage(): number {
    if (this.stats && this.stats.totalAttendu > 0) {
      return (this.stats.totalPaye / this.stats.totalAttendu) * 100;
    }
    return 0;
  }
}
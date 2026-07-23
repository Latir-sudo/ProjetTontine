// pages/historiques/historiques.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaiementService } from '../../services/paiement.service';
import { PaiementHistorique, PaiementStats } from '../../models/paiement.model';
import { AuthService } from '../../services/auth.services';

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

  private currentUserId: number | null = null;

  constructor(
    private paiementService: PaiementService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    const user = this.authService.currentUser();
    this.currentUserId = user?.id ?? null;
    this.loadHistorique();
    this.loadStats();
    }

  loadHistorique(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.cdr.detectChanges();

    if (!this.currentUserId) {
      this.payments = [];
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.paiementService.getHistoriqueByUser(this.currentUserId).subscribe({
      next: (data) => {
        this.payments = data || [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.payments = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadStats(): void {
    if (!this.currentUserId) {
      this.stats = null;
      return;
    }

    this.paiementService.getStatsByUser(this.currentUserId).subscribe({
      next: (data) => {
        this.stats = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.stats = null;
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
    if (payment.valide === false) return 'En attente';
    return 'Échoué';
  }

  getStatusClass(payment: PaiementHistorique): string {
    if (payment.valide === true) return 'success';
    if (payment.valide === false) return 'pending';
    return 'failed';
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
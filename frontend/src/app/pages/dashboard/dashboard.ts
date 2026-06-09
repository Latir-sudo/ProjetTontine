// dashboard.component.ts
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd, Event as RouterEvent } from '@angular/router';
import { AuthService } from '../../services/auth.services';
import { ApiService } from '../../services/api.service';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';

interface Tontine {
  id: number;
  nomTontine: string;
  descriptionTontine?: string;
  montant: number;
  frequence?: string;
  region?: string;
  categorie?: string;
  nombreMembres?: number;
  idAdmin?: number;
  statutTontine?: string;
  nombreMax?: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class Dashboard implements OnInit, OnDestroy {
  currentUser: any = null;
  mesTontines: Tontine[] = [];
  tontinesDisponibles: Tontine[] = [];
  totalCotise = 0;
  isLoading = true;
  private routerSubscription: Subscription;
  private refreshInterval: any;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    // Écouter les événements de navigation
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(async (event: NavigationEnd) => {
        // Vérifier si on est revenu sur le dashboard
        if (event.url === '/dashboard' || event.url === '/') {
          console.log('🔄 Retour au dashboard, rechargement des données...');
          await this.refreshData();
          this.cdr.detectChanges();
        }
      });
  }

  async ngOnInit() {
    console.log('🚀 Dashboard initialisé');
    await this.initializeDashboard();
    
    // Rafraîchir toutes les 30 secondes
    this.refreshInterval = setInterval(() => {
      if (this.router.url === '/dashboard') {
        console.log('⏰ Rafraîchissement automatique...');
        this.refreshData();
      }
    }, 30000);
  }

  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  private async initializeDashboard() {
    // Utilisation correcte du signal (fonction)
    this.currentUser = this.authService.currentUser();
    
    console.log('=== DASHBOARD INIT ===');
    console.log('Utilisateur:', this.currentUser?.email, 'ID:', this.currentUser?.id);
    
    if (!this.currentUser || !this.currentUser.id) {
      console.error('❌ Pas d\'utilisateur valide');
      this.router.navigate(['/login']);
      return;
    }

    await this.refreshData();
    this.cdr.detectChanges();
  }

  private async refreshData() {
    console.log('🔄 Rafraîchissement des données...');
    this.isLoading = true;
    try {
      await Promise.all([
        this.loadMesTontines(),
        this.loadTontinesDisponibles(),
        this.loadTotalCotisations()
      ]);
      console.log('✅ Données rafraîchies avec succès');
    } catch (error) {
      console.error('❌ Erreur lors du rafraîchissement:', error);
    } finally {
      this.isLoading = false;
    }
  }

  private async loadMesTontines() {
    try {
      console.log('📡 Chargement des tontines...');
      const response = await this.apiService.get<Tontine[]>('/tontine/mes-tontines');
      
      if (response && Array.isArray(response)) {
        this.mesTontines = response;
        console.log(`✅ ${this.mesTontines.length} tontine(s) chargée(s)`);
        
        // Stocker dans sessionStorage pour persistance
        sessionStorage.setItem('mesTontines', JSON.stringify(this.mesTontines));
        sessionStorage.setItem('mesTontines_timestamp', Date.now().toString());
      } else {
        this.restoreFromCache();
      }
    } catch (error) {
      console.error('❌ Erreur chargement:', error);
      this.restoreFromCache();
    }
  }

  private restoreFromCache() {
    const cached = sessionStorage.getItem('mesTontines');
    const timestamp = sessionStorage.getItem('mesTontines_timestamp');
    
    if (cached && timestamp) {
      const age = Date.now() - parseInt(timestamp);
      // Utiliser le cache si moins de 5 minutes
      if (age < 5 * 60 * 1000) {
        this.mesTontines = JSON.parse(cached);
        console.log('📦 Données restaurées du cache');
      } else {
        this.mesTontines = [];
      }
    } else {
      this.mesTontines = [];
    }
  }

  private async loadTontinesDisponibles() {
    try {
      const allTontines = await this.apiService.get<Tontine[]>('/tontine');
      
      if (allTontines && Array.isArray(allTontines)) {
        const mesTontineIds = new Set(this.mesTontines.map(t => t.id));
        this.tontinesDisponibles = allTontines.filter(t => !mesTontineIds.has(t.id));
        console.log('Tontines disponibles:', this.tontinesDisponibles.length);
      }
    } catch (error) {
      console.error('Erreur chargement disponibles:', error);
      this.tontinesDisponibles = [];
    }
  }

  private async loadTotalCotisations() {
    try {
      if (!this.currentUser?.id || this.mesTontines.length === 0) {
        this.totalCotise = 0;
        return;
      }
      
      let total = 0;
      
      for (const tontine of this.mesTontines) {
        try {
          const cotisations = await this.apiService.get<any[]>(`/cotisations/user/${this.currentUser.id}/tontine/${tontine.id}`);
          
          if (cotisations && Array.isArray(cotisations)) {
            total += cotisations.reduce((sum, c) => sum + (c.montant || 0), 0);
          }
        } catch (error) {
          // Pas de cotisations pour cette tontine
        }
      }
      
      this.totalCotise = total;
      console.log(`Total cotisé: ${this.totalCotise} FCFA`);
      
    } catch (error) {
      console.error('Erreur chargement cotisations:', error);
      this.totalCotise = 0;
    }
  }

  async postulerAdhesion(tontineId: number) {
    if (!confirm('Voulez-vous rejoindre cette tontine ?')) {
      return;
    }
    
    try {
      this.isLoading = true;
      
      const adhesionRequest = { idTontine: tontineId };
      await this.apiService.post('/tontine/adhesion', adhesionRequest);
      
      alert('✅ Demande d\'adhésion envoyée avec succès !');
      
      // Recharger immédiatement
      await this.refreshData();
      
    } catch (error: any) {
      console.error('❌ Erreur adhésion:', error);
      alert('Erreur: ' + (error.error?.message || error.message));
    } finally {
      this.isLoading = false;
    }
  }

  getProchainPaiement(tontineId: number): number {
    return 7;
  }
}
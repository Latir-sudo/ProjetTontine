import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.services';

interface Tontine {
  id: number;
  nomTontine: string;
  descriptionTontine?: string;
  montant: number;
  frequence?: string;
  region?: string;
  categorie?: string;
  nombreMembres?: number;
  statutTontine?: string;
}

@Component({
  selector: 'app-available-tontines',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './available-tontines.html',
  styleUrls: ['./available-tontines.scss']
})
export class AvailableTontines implements OnInit {
  
  // Données
  allTontines: Tontine[] = [];
  filteredTontines: Tontine[] = [];
  mesTontinesIds: number[] = [];
  isLoading = true;
  
  // Filtres et recherche
  searchText = '';
  selectedFilter = 'Toutes';
  sortBy = 'recent';
  showFilters = false;
  selectedTontineId: number | null = null;

  // Catégories (seront remplies dynamiquement)
  filters: string[] = ['Toutes'];
  
  sortOptions = [
    { value: 'recent', label: 'Plus récent' },
    { value: 'members', label: 'Plus de membres' },
    { value: 'amount', label: 'Montant (croissant)' },
    { value: 'amountDesc', label: 'Montant (décroissant)' }
  ];

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    console.log('[DEBUG] ngOnInit appelé');
    await this.loadData();
    this.cdr.detectChanges();
    console.log('[DEBUG] Après loadData, filteredTontines:', this.filteredTontines);
  }
  private async loadData() {
    this.isLoading = true;
    this.cdr.detectChanges();
    console.log('[DEBUG] Début loadData');
    try {
      // 1. Charger toutes les tontines
      const allTontines = await this.apiService.get<Tontine[]>('/tontine');
      this.allTontines = allTontines || [];
      console.log('✅ Tontines chargées:', this.allTontines.length);
      // 2. Extraire les catégories uniques
      this.extractCategories();
      // 3. Charger les IDs des tontines de l'utilisateur
      await this.loadUserTontineIds();
      console.log('[DEBUG] Après loadUserTontineIds, mesTontinesIds:', this.mesTontinesIds);
      // 4. ✅ CRUCIAL : Appliquer les filtres APRÈS le chargement
      this.applyFilters();
      console.log('[DEBUG] Après applyFilters, filteredTontines:', this.filteredTontines.length);
    } catch (error) {
      console.error('❌ Erreur chargement:', error);
      this.allTontines = [];
      this.filteredTontines = [];
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
      console.log('[DEBUG] Fin loadData, isLoading:', this.isLoading);
    }
  }

  private async loadUserTontineIds() {
    try {
      const mesTontines = await this.apiService.get<Tontine[]>('/tontine/mes-tontines');
      this.mesTontinesIds = mesTontines.map(t => t.id);
      console.log('✅ Mes tontines IDs:', this.mesTontinesIds);
    } catch (error) {
      console.log('Aucune tontine trouvée pour cet utilisateur');
      this.mesTontinesIds = [];
    }
  }

  // Extraire les catégories uniques depuis les tontines chargées
  private extractCategories() {
    const categoriesSet = new Set<string>();
    
    this.allTontines.forEach(tontine => {
      if (tontine.categorie && tontine.categorie.trim() !== '') {
        categoriesSet.add(tontine.categorie);
      }
    });
    
    const categories = Array.from(categoriesSet).sort();
    this.filters = ['Toutes', ...categories];
    
    console.log('📋 Catégories disponibles:', this.filters);
  }

 applyFilters() {
  console.log('🟢 applyFilters() - allTontines:', this.allTontines.length);
  console.log('🟢 applyFilters() - mesTontinesIds:', this.mesTontinesIds);
  
  // Partir de toutes les tontines
  let result = [...this.allTontines];
  
  // 1. Exclure celles où l'utilisateur est déjà membre
  result = result.filter(t => !this.mesTontinesIds.includes(t.id));
  
  // 2. Filtrer par catégorie
  if (this.selectedFilter !== 'Toutes') {
    result = result.filter(t => t.categorie === this.selectedFilter);
  }

  // 3. Filtrer par recherche textuelle
  if (this.searchText.trim()) {
    const search = this.searchText.toLowerCase();
    result = result.filter(t =>
      t.nomTontine?.toLowerCase().includes(search) ||
      t.region?.toLowerCase().includes(search) ||
      t.descriptionTontine?.toLowerCase().includes(search)
    );
  }

  // 4. Trier
  result = this.sortTontines(result);
  
  this.filteredTontines = result;
  console.log('🎯 Tontines affichées:', this.filteredTontines.length);
}
  sortTontines(tontines: Tontine[]): Tontine[] {
    const sorted = [...tontines];
    switch (this.sortBy) {
      case 'members':
        return sorted.sort((a, b) => (b.nombreMembres || 0) - (a.nombreMembres || 0));
      case 'amount':
        return sorted.sort((a, b) => a.montant - b.montant);
      case 'amountDesc':
        return sorted.sort((a, b) => b.montant - a.montant);
      default:
        return sorted;
    }
  }

  toggleFilters() {
    this.showFilters = !this.showFilters;
  }

  setFilter(filter: string) {
    this.selectedFilter = filter;
    this.applyFilters();
  }

  onSortChange() {
    this.applyFilters();
  }

  onSearchChange() {
    this.applyFilters();
  }

  toggleTontineDetails(id: number) {
    this.selectedTontineId = this.selectedTontineId === id ? null : id;
  }

  async postulerAdhesion(tontineId: number) {
    try {
      await this.apiService.post('/tontine/adhesion', { idTontine: tontineId });
      alert('✅ Demande d\'adhésion envoyée avec succès !');
      await this.loadData(); // Recharger pour mettre à jour la liste
    } catch (error) {
      console.error('❌ Erreur adhésion:', error);
      alert('Erreur lors de la demande d\'adhésion');
    }
  }

  viewDetails(tontine: Tontine) {
    this.router.navigate(['/tontine', tontine.id]);
  }

  isJoined(id: number): boolean {
    return this.mesTontinesIds.includes(id);
  }
}
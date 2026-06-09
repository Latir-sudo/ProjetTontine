import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';

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
  nombreMax?: number;
  statutTontine?: string;
  dateCreation?: string;
}

@Component({
  selector: 'app-mes-tontines',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './mes-tontines.html',
  styleUrls: ['./mes-tontines.scss']
})
export class MesTontines implements OnInit {
  
  // Données
  allTontines: Tontine[] = [];
  filteredTontines: Tontine[] = [];
  isLoading = true;
  
  // Filtres et recherche
  searchText = '';
  selectedFilter = 'Toutes';
  showFilters = false;
  
  // Types de filtres (catégories)
  filters: string[] = ['Toutes'];
  
  // Options de tri
  sortBy = 'recent';
  sortOptions = [
    { value: 'recent', label: 'Plus récent' },
    { value: 'amountAsc', label: 'Montant (croissant)' },
    { value: 'amountDesc', label: 'Montant (décroissant)' },
    { value: 'members', label: 'Plus de membres' }
  ];

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    this.cdr.detectChanges();
    await this.loadData();
  }

  private async loadData() {
    this.isLoading = true;
    try {
      // Charger les tontines de l'utilisateur connecté
      const tontines = await this.apiService.get<Tontine[]>('/tontine/mes-tontines');
      this.allTontines = tontines || [];
      
      // Extraire les catégories uniques
      this.extractCategories();
      
      // Appliquer les filtres
      this.applyFilters();
      
      console.log('Mes tontines chargées:', this.allTontines.length);
    } catch (error) {
      console.error('Erreur chargement mes tontines:', error);
      this.allTontines = [];
      this.filteredTontines = [];
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private extractCategories() {
    const categoriesSet = new Set<string>();
    
    this.allTontines.forEach(tontine => {
      if (tontine.categorie && tontine.categorie.trim() !== '') {
        categoriesSet.add(tontine.categorie);
      }
    });
    
    const categories = Array.from(categoriesSet).sort();
    this.filters = ['Toutes', ...categories];
  }

  applyFilters() {
    let result = [...this.allTontines];
    
    // Filtre par catégorie
    if (this.selectedFilter !== 'Toutes') {
      result = result.filter(t => t.categorie === this.selectedFilter);
    }
    
    // Filtre par recherche textuelle
    if (this.searchText.trim()) {
      const search = this.searchText.toLowerCase();
      result = result.filter(t =>
        t.nomTontine?.toLowerCase().includes(search) ||
        t.region?.toLowerCase().includes(search) ||
        t.descriptionTontine?.toLowerCase().includes(search)
      );
    }
    
    // Tri
    result = this.sortTontines(result);
    
    this.filteredTontines = result;
  }

  sortTontines(tontines: Tontine[]): Tontine[] {
    const sorted = [...tontines];
    switch (this.sortBy) {
      case 'amountAsc':
        return sorted.sort((a, b) => a.montant - b.montant);
      case 'amountDesc':
        return sorted.sort((a, b) => b.montant - a.montant);
      case 'members':
        return sorted.sort((a, b) => (b.nombreMembres || 0) - (a.nombreMembres || 0));
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

  onSearchChange() {
    this.applyFilters();

  }

  onSortChange() {
    this.applyFilters();

  }

  viewDetails(tontineId: number) {
    this.router.navigate(['/tontine', tontineId]);
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
  }
}
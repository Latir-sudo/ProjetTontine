import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

interface Tontine {
  id: number;
  nomTontine: string;
  descriptionTontine?: string;
  montant: number;
  frequence?: string;
  region?: string;
  categorie?: string;
  nombreMembres?: number;
  maxMembres?: number;
  statutTontine?: string;
}

@Component({
  selector: 'app-accueil',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './accueil.html',
  styleUrls: ['./accueil.scss']
})
export class Accueil implements OnInit {
  
  // Signaux pour la réactivité
  allTontines = signal<Tontine[]>([]);
  filteredTontines = signal<Tontine[]>([]);
  isLoading = signal(true);
  
  // Filtres
  searchText = '';
  selectedType = '';
  selectedTontineId = signal<number | null>(null);
  
  // Types de tontines (extraits des données)
  typesList = signal<string[]>([]);

  constructor(private apiService: ApiService) {}

  async ngOnInit() {
    await this.loadTontines();
  }

  private async loadTontines() {
    this.isLoading.set(true);
    try {
      // Récupérer toutes les tontines actives
      const tontines = await this.apiService.get<Tontine[]>('/tontine');
      
      // Ne garder que les tontines actives
      const activeTontines = (tontines || []).filter(t => t.statutTontine !== 'INACTIVE');
      
      this.allTontines.set(activeTontines);
      
      // Extraire les types uniques (catégories)
      const types = [...new Set(activeTontines.map(t => t.categorie).filter((c): c is string => !!c))];
      this.typesList.set(types);
      
      // Appliquer les filtres
      this.applyFilters();
      
    } catch (error) {
      console.error('Erreur chargement tontines:', error);
      this.allTontines.set([]);
      this.filteredTontines.set([]);
    } finally {
      this.isLoading.set(false);
    }
  }

  applyFilters() {
    let result = [...this.allTontines()];
    
    // Filtre par type (catégorie)
    if (this.selectedType) {
      result = result.filter(t => t.categorie === this.selectedType);
    }
    
    // Filtre par recherche
    if (this.searchText.trim()) {
      const search = this.searchText.toLowerCase();
      result = result.filter(t =>
        t.nomTontine?.toLowerCase().includes(search) ||
        t.region?.toLowerCase().includes(search) ||
        t.descriptionTontine?.toLowerCase().includes(search)
      );
    }
    
    this.filteredTontines.set(result);
  }

  onSearchChange() {
    this.applyFilters();
  }

  onTypeChange() {
    this.applyFilters();
  }

  getTypes(): string[] {
    return this.typesList();
  }

  toggleTontineDetails(id: number) {
    if (this.selectedTontineId() === id) {
      this.selectedTontineId.set(null);
    } else {
      this.selectedTontineId.set(id);
    }
  }

  getProgressPercentage(tontine: Tontine): number {
    if (!tontine.nombreMembres) return 0;
    const max = tontine.maxMembres || 20;
    const percentage = (tontine.nombreMembres / max) * 100;
    return Math.min(percentage, 100);
  }
}
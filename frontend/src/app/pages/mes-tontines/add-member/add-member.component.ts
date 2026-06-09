import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ApiService } from '../../../services/api.service';

interface Utilisateurs {
  id?: number;
  nom: string;
  prenom: string;
  telephone: string;
  email: string;
}

interface AdhesionRequest {
  idUser?: number;
  dateAdhesion?: string;
}

interface MembreTontine {
  idUser: number;
  idTontine: number;
}

@Component({
  selector: 'app-add-member',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-member.component.html',
  styleUrls: ['./add-member.component.scss']
})
export class AddMemberComponent implements OnInit {

  isLoading = false;
  errorMessage = '';
  successMessage = '';
  isSearching = false;
  utilisateurs: Utilisateurs[] = [];
  existingMemberIds = new Set<number>();

  // ✅ filteredUtilisateurs doit être un TABLEAU
  filteredUtilisateurs: Utilisateurs[] = [];

  searchText = '';

  constructor(
    private apiService: ApiService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    await this.loadData();
    this.cdr.detectChanges();
  }

  private async loadData() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    try {
      const tontineId = this.route.snapshot.paramMap.get('id');
      const [utilisateurs] = await Promise.all([
        this.apiService.get<Utilisateurs[]>('/users'),
        tontineId ? this.loadExistingMembers(tontineId) : Promise.resolve()
      ]);

      this.utilisateurs = utilisateurs;
      console.log('[DEBUG] Utilisateurs chargés:', this.utilisateurs.length);

      this.filteredUtilisateurs = [];

      if (this.utilisateurs.length === 0) {
        this.errorMessage = 'Aucun utilisateur trouvé';
      }
    } catch (error) {
      console.error('[DEBUG] Erreur lors du chargement:', error);
      this.utilisateurs = [];
      this.filteredUtilisateurs = [];
      this.errorMessage = 'Erreur lors du chargement des utilisateurs';
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private async loadExistingMembers(tontineId: string) {
    try {
      const members = await this.apiService.get<MembreTontine[]>(`/membres/tontine/${tontineId}`);
      this.existingMemberIds = new Set((members || []).map(member => member.idUser));
    } catch (error) {
      console.error('[DEBUG] Erreur chargement membres existants:', error);
      this.existingMemberIds = new Set<number>();
    }
  }

  async applyTelephoneFilter() {
    const search = this.searchText?.trim() || '';

    if (search === '') {
      this.resetFilter();
      return;
    }

    if (search.length < 6) {
      this.errorMessage = 'Numéro de téléphone invalide (minimum 6 chiffres)';
      this.filteredUtilisateurs = [];
      return;
    }

    this.isSearching = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    try {
      const encodedSearch = encodeURIComponent(search);
      const result = await this.apiService.get<any>(`/users/search?telephone=${encodedSearch}`);

      console.log('[DEBUG] Résultat brut:', result);

      if (Array.isArray(result)) {
        this.filteredUtilisateurs = result;
      } else if (result && typeof result === 'object') {
        this.filteredUtilisateurs = [result];
      } else {
        this.filteredUtilisateurs = [];
      }

      if (this.filteredUtilisateurs.length === 0) {
        this.errorMessage = `Aucun utilisateur trouvé avec le numéro "${search}"`;
      }

      console.log('[DEBUG] Utilisateurs filtrés:', this.filteredUtilisateurs.length);

    } catch (error) {
      console.error('[DEBUG] Erreur filtrage:', error);
      this.errorMessage = 'Utilisateur non trouvé';
      this.filteredUtilisateurs = [];
    } finally {
      this.isSearching = false;
      this.cdr.detectChanges();
    }
  }

  resetFilter() {
    this.searchText = '';
    this.filteredUtilisateurs = [];
    this.errorMessage = '';
    this.cdr.detectChanges();
  }

  // ✅ Méthode corrigée pour ajouter un membre à la tontine
  async addMember(member: Utilisateurs) {
    if (this.isAlreadyMember(member)) {
      return;
    }

    // Récupérer l'ID de la tontine depuis l'URL
    const tontineId = this.route.snapshot.paramMap.get('id');

    if (!tontineId) {
      this.errorMessage = 'ID de la tontine manquant';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.cdr.detectChanges();

    // ✅ Construire l'objet AdhesionRequest
    const adhesionRequest: AdhesionRequest = {
      idUser: member.id,
      dateAdhesion: new Date().toISOString().split('T')[0] // Format YYYY-MM-DD
    };

    try {
      await this.apiService.post(`/tontine/${tontineId}/ajouter`, adhesionRequest);
      if (member.id) {
        this.existingMemberIds.add(member.id);
      }
      this.successMessage = `${member.prenom} ${member.nom} a été ajouté avec succès !`;

      // ✅ Réinitialiser après succès
      setTimeout(() => {
        this.resetFilter();
        this.successMessage = '';
        this.router.navigate(['/tontine', tontineId]);
      }, 2000);

    } catch (error) {
      console.error('Erreur ajout membre:', error);
      this.errorMessage = 'Erreur lors de l\'ajout du membre';
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  goBack() {
    this.router.navigate(['/mes-tontines']);
  }

  isAlreadyMember(user: Utilisateurs): boolean {
    return !!user.id && this.existingMemberIds.has(user.id);
  }
}

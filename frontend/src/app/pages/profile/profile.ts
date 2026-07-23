import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.services';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class Profile {
  user: User | null = null;
  editingUser: User | null = null;
  saving = false;
  message = '';
  errors: { prenom?: string; nom?: string; email?: string; telephone?: string } = {};
  avatarPreview: string | null = null;
  private selectedAvatarBase64: string | null = null;

  constructor(private auth: AuthService, private api: ApiService, private router: Router, private cdr: ChangeDetectorRef) {
    this.user = this.auth.currentUser();
    this.editingUser = this.user ? { ...this.user } : null;
    this.avatarPreview = this.user?.avatar || null;
    this.loadUserFromBackend();
  }

  private async loadUserFromBackend() {
    if (!this.user?.id) return;
    try {
      const data: any = await this.api.get(`/users/${this.user.id}`);
      const loaded: User = {
        id: data.id,
        prenom: data.prenom,
        nom: data.nom,
        email: data.email,
        telephone: data.telephone,
        ville: data.ville,
        avatar: data.avatar || '',
        roles: data.roles || [],
        dateInscription: data.dateInscription
      };
      this.user = loaded;
      this.editingUser = { ...loaded };
      this.avatarPreview = loaded.avatar || null;
      this.auth.currentUser.set(loaded);
    } catch (err) {
      console.error('Erreur chargement profil', err);
    }
  }

  async save() {
    if (!this.editingUser) return;
    this.errors = {};
    if (!this.validate()) return;
    this.saving = true;
    this.message = '';
    try {
      const payload: any = {
        prenom: this.editingUser.prenom,
        nom: this.editingUser.nom,
        email: this.editingUser.email,
        telephone: this.editingUser.telephone,
        ville: this.editingUser.ville,
        avatar: this.selectedAvatarBase64 || undefined
      };

      const updated: any = await this.api.patch(`/users/${this.editingUser.id}`, payload);
      // Mettre à jour l'utilisateur courant côté client
      const newUser: User = {
        id: updated.id,
        prenom: updated.prenom,
        nom: updated.nom,
        email: updated.email,
        telephone: updated.telephone,
        ville: updated.ville,
        avatar: updated.avatar || '',
        roles: updated.roles || [],
        dateInscription: updated.dateInscription
      };
      this.auth.currentUser.set(newUser);
      this.user = newUser;
      this.editingUser = { ...newUser };
      this.message = 'Profil mis à jour avec succès.';
    } catch (err: any) {
      console.error('Erreur mise à jour profil', err);
      this.message = err?.error?.message || 'Erreur lors de la mise à jour du profil.';
    } finally {
      this.saving = false;
      this.cdr.detectChanges();
    }
  }

  validate(): boolean {
    if (!this.editingUser) return false;
    let ok = true;
    if (!this.editingUser.prenom || this.editingUser.prenom.trim().length < 2) {
      this.errors.prenom = 'Prénom trop court';
      ok = false;
    }
    if (!this.editingUser.nom || this.editingUser.nom.trim().length < 2) {
      this.errors.nom = 'Nom trop court';
      ok = false;
    }
    if (!this.editingUser.email || !/^\S+@\S+\.\S+$/.test(this.editingUser.email)) {
      this.errors.email = 'Email invalide';
      ok = false;
    }
    if (!this.editingUser.telephone || !/^[0-9+ \-]{6,20}$/.test(this.editingUser.telephone)) {
      this.errors.telephone = 'Téléphone invalide';
      ok = false;
    }
    return ok;
  }

  cancel() {
    this.editingUser = this.user ? { ...this.user } : null;
    this.errors = {};
    this.message = '';
    this.avatarPreview = this.user?.avatar || null;
    this.selectedAvatarBase64 = null;
  }

  handleFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    if (file.size > 500_000) {
      this.message = 'Image trop volumineuse (max 500 Ko).';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      this.avatarPreview = reader.result as string;
      this.selectedAvatarBase64 = reader.result as string;
    };
    reader.readAsDataURL(file);
  }
}

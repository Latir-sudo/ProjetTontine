// auth.services.ts - Version corrigée
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  ville: string;
  roles: string[];
  dateInscription?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  
  currentUser = signal<User | null>(null);
  isLoggedIn = signal(false);

  /**
   * Retourne true si l'utilisateur est connecté (token valide en localStorage ou signal true)
   */
  isLoggedInNow(): boolean {
    const token = this.getToken();
    if (token && !this.isTokenExpired(token)) {
      return true;
    }
    return this.isLoggedIn();
  }

  constructor(private http: HttpClient, private router: Router) {
    this.loadStoredData();
  }

  private loadStoredData() {
    const token = localStorage.getItem('token');
    console.log('🔐 AuthService: chargement token depuis localStorage:', !!token);
    
    if (token && !this.isTokenExpired(token)) {
      const user = this.decodeUserFromToken(token);
      if (user && user.id) {
        this.currentUser.set(user);
        this.isLoggedIn.set(true);
        console.log('✅ AuthService: utilisateur rechargé:', user.email, 'ID:', user.id);
      } else {
        console.error('❌ AuthService: impossible de décoder l\'utilisateur depuis le token');
        this.clearAuthData();
      }
    } else {
      console.log('⚠️ AuthService: token invalide ou expiré');
      this.clearAuthData();
    }
  }

  private decodeUserFromToken(token: string): User | null {
    try {
      const decoded: any = jwtDecode(token);
      console.log('📦 Token décodé:', decoded);
      
      // Vérifiez où se trouvent les données utilisateur dans votre token
      // Soit dans decoded.user, soit directement dans decoded
      const userData = decoded.user || decoded;
      
      if (!userData) {
        console.error('Token ne contient pas de données utilisateur');
        return null;
      }
      
      // S'assurer que l'ID est présent
      if (!userData.id) {
        console.error('Token ne contient pas d\'ID utilisateur');
        return null;
      }
      
      const user: User = {
        id: userData.id,
        nom: userData.nom || '',
        prenom: userData.prenom || '',
        email: userData.email || '',
        telephone: userData.telephone || '',
        ville: userData.ville || userData.localite || '',
        roles: userData.roles || [],
        dateInscription: userData.dateInscription
      };
      
      console.log('👤 Utilisateur décodé:', user);
      return user;
    } catch (error) {
      console.error('Erreur décodage token:', error);
      return null;
    }
  }

  public isTokenExpired(token: string): boolean {
    try {
      const decoded: any = jwtDecode(token);
      const exp = decoded.exp;
      const now = Date.now() / 1000;
      const isExpired = exp < now;
      if (isExpired) {
        console.log('Token expiré depuis', new Date(exp * 1000));
      }
      return isExpired;
    } catch {
      return true;
    }
  }

  private clearAuthData() {
    localStorage.removeItem('token');
    this.currentUser.set(null);
    this.isLoggedIn.set(false);
  }

  async login(email: string, password: string): Promise<{ success: boolean; message: string }> {
    try {
      console.log('🔑 Tentative de login pour:', email);
      const response = await firstValueFrom(
        this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
      );
      
      console.log('📥 Réponse login:', response);
      
      if (response.success && response.token) {
        localStorage.setItem('token', response.token);
        const user = this.decodeUserFromToken(response.token);
        
        if (user && user.id) {
          this.currentUser.set(user);
          this.isLoggedIn.set(true);
          console.log('✅ Login réussi pour:', user.email, 'ID:', user.id);
          return { success: true, message: response.message };
        } else {
          console.error('❌ Impossible d\'extraire l\'utilisateur du token');
          return { success: false, message: 'Erreur lors de la connexion' };
        }
      }
      return { success: false, message: response.message };
    } catch (error: any) {
      console.error('Login error:', error);
      return { 
        success: false, 
        message: error.error?.message || 'Email ou mot de passe incorrect' 
      };
    }
  }

  async register(userData: any): Promise<{ success: boolean; message: string }> {
    try {
      const response = await firstValueFrom(
        this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData)
      );
      
      if (response.success && response.token) {
        localStorage.setItem('token', response.token);
        const user = this.decodeUserFromToken(response.token);
        
        if (user) {
          this.currentUser.set(user);
          this.isLoggedIn.set(true);
          return { success: true, message: response.message };
        }
      }
      return { success: false, message: response.message };
    } catch (error: any) {
      console.error('Register error:', error);
      return { 
        success: false, 
        message: error.error?.message || 'Erreur lors de l\'inscription' 
      };
    }
  }

  logout() {
    localStorage.removeItem('token');
    this.currentUser.set(null);
    this.isLoggedIn.set(false);
    this.router.navigate(['/login']);
  }

  isAdmin(): boolean {
    const user = this.currentUser();
    return user?.roles?.includes('ADMIN') ?? false;
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
  
  // Nouvelle méthode pour forcer le rechargement de l'utilisateur
  refreshUser(): User | null {
    this.loadStoredData();
    return this.currentUser();
  }
}
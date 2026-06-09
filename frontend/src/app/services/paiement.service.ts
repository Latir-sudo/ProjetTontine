// services/paiement.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaiementHistorique, PaiementStats } from '../models/paiement.model';

@Injectable({
  providedIn: 'root'
})
export class PaiementService {
  private apiUrl = 'http://localhost:8080/api/paiements';

  constructor(private http: HttpClient) { }

  getHistoriqueByMembre(membreId: number): Observable<PaiementHistorique[]> {
    return this.http.get<PaiementHistorique[]>(`${this.apiUrl}/membre/${membreId}/historique`);
  }

  getStatsByMembre(membreId: number): Observable<PaiementStats> {
    return this.http.get<PaiementStats>(`${this.apiUrl}/membre/${membreId}/stats`);
  }
}
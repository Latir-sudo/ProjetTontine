// models/paiement.model.ts

export enum ModePaiement {
  ORANGE_MONEY = 'ORANGE_MONEY',
  WAVE = 'WAVE',
  FREE_MONEY = 'FREE_MONEY'
}

export interface PaiementHistorique {
  id: number;
  montant: number;
  datePaiement: string;
  modePaiement: ModePaiement;
  reference: string;
  valide: boolean | null;  // true = réussi, false = échoué, null = en attente
  titreCotisation: string;
  cotisationId: number;
  membreNom: string;
  membrePrenom: string;
  nomTontine: string;
}

export interface PaiementStats {
  totalPaye: number;
  totalAttendu: number;
  paiementsReussis: number;
  paiementsEnAttente: number;
  paiementsEchoues: number;
  tauxCompletude: number;
}

export interface WaveCheckoutResponse {
  success: boolean;
  transactionId: string;
  provider: 'WAVE';
  message: string;
  amount: number;
  reference: string;
  status: 'PENDING' | 'SUCCESS';
  launchUrl: string | null;
}

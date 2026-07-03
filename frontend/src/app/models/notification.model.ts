// src/app/models/notification.model.ts
export interface Notification {
  id: number;
  titre: string;
  message: string;
  tempsRelatif: string;
  couleur: string;
  estLu: boolean;
  idTontine?: number;
  nomTontine?: string;
  idUser?: number;
  prenomUser?: string;
  nomUser?: string;
  dateCreation: string;
  statutNotification?: string;
  typeNotification: string;
  lienAction?: string;
}

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
  // dateCreation peut être un ISO string "2024-07-22T10:30:00"
  // ou un tableau Java [year, month, day, hour, min, sec]
  dateCreation: string | number[];
  statutNotification?: string;
  typeNotification: string;
  lienAction?: string;
}

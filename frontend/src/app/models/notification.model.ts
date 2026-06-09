// src/app/models/notification.model.ts
export interface Notification {
  id: number;
  titre: string;
  message: string;
  tempsRelatif: string;
  couleur: string;
  estLu: boolean;
  dateCreation: string;
  typeNotification: string;
  lienAction?: string;
}

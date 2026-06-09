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

export interface UserRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  password: string;
  ville: string;
  roles: string[];
}

export interface AuthResponse {
  success: boolean;
  message: string;
  token: string;
}
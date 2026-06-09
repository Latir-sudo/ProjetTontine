import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { TontineCreate } from './pages/create-tontine/create-tontine';
import { Paiement } from './pages/paiement/paiement';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Accueil } from './pages/accueil/accueil';  // ← Importer le composant Home
import { AuthGuard } from './guards/auth.guard';
import { AvailableTontines } from './pages/available-tontines/available-tontines';
import { Historiques } from './pages/historiques/historiques';
import { DetailTontine } from './pages/detail-tontine/detail-tontine';
import { MesTontines } from './pages/mes-tontines/mes-tontines';
import { Administration } from './pages/administration/administration';
import { AddMemberComponent } from './pages/mes-tontines/add-member/add-member.component';
import { NotificationsComponent } from './pages/notifications/notifications.component';
export const routes: Routes = [
  
  { path: 'accueil', component: Accueil },
  { path: 'tontine/gestion-membres/:id', component: AddMemberComponent, canActivate: [AuthGuard] }, // Route pour la gestion des membres d'une tontine spécifique
  { path: 'notifications', component: NotificationsComponent, canActivate: [AuthGuard] },
  
  // Routes d'authentification (publiques)
  { path: 'login', component: Login },
  { path: 'inscription', component: Register },
  
  // Routes protégées (nécessitent authentification)
  { path: 'dashboard', component: Dashboard, canActivate: [AuthGuard] },
  { path: 'tontine-availables', component: AvailableTontines},
  { path: 'historique', component:Historiques, canActivate: [AuthGuard] }, 
  { path: 'tontine/create', component: TontineCreate, canActivate: [AuthGuard] },
  { path: 'paiement/:id', component: Paiement, canActivate: [AuthGuard] },
  { path: 'tontine/:id', component: DetailTontine, canActivate: [AuthGuard] },
  { path: 'mes-tontines', component: MesTontines, canActivate: [AuthGuard] },
  { path: 'admin/:id', component: Administration, canActivate: [AuthGuard] }, // Route pour la gestion admin d'une tontine spécifique
  
  // Redirections
  { path: '', redirectTo: '/accueil', pathMatch: 'full' },
  { path: '**', redirectTo: '/accueil' }
];
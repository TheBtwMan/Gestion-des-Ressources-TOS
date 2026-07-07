import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login').then((m) => m.LoginComponent),
  },
  {
    path: '',
    loadComponent: () => import('./layout/shell').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.DashboardComponent),
      },
      {
        path: 'parametrage/mode-travail',
        loadComponent: () => import('./features/parametrage/mode-travail').then((m) => m.ModeTravailComponent),
      },
      {
        path: 'parametrage/periode-shift',
        loadComponent: () => import('./features/parametrage/periode-shift').then((m) => m.PeriodeShiftComponent),
      },
      {
        path: 'parametrage/equipes',
        loadComponent: () => import('./features/parametrage/equipes').then((m) => m.EquipesComponent),
      },
      {
        path: 'parametrage/plan-roulement',
        loadComponent: () => import('./features/parametrage/plan-roulement').then((m) => m.PlanRoulementComponent),
      },
      {
        path: 'parametrage/mains-theoriques',
        loadComponent: () => import('./features/parametrage/mains-theoriques').then((m) => m.MainsTheoriquesComponent),
      },
      {
        path: 'parametrage/normes-productivite',
        loadComponent: () => import('./features/parametrage/normes-productivite').then((m) => m.NormesProductiviteComponent),
      },
      {
        path: 'exploitation/escales',
        loadComponent: () => import('./features/exploitation/escales').then((m) => m.EscalesComponent),
      },
      {
        path: 'exploitation/commandes',
        loadComponent: () => import('./features/exploitation/commandes').then((m) => m.CommandesComponent),
      },
      {
        path: 'exploitation/commandes/:numero',
        loadComponent: () => import('./features/exploitation/commande-detail').then((m) => m.CommandeDetailComponent),
      },
      {
        path: 'exploitation/absences-arrets',
        loadComponent: () => import('./features/exploitation/absences-arrets').then((m) => m.AbsencesArretsComponent),
      },
      {
        path: 'admin/profils',
        loadComponent: () => import('./features/admin/profils').then((m) => m.ProfilsComponent),
      },
      {
        path: 'admin/utilisateurs',
        loadComponent: () => import('./features/admin/utilisateurs').then((m) => m.UtilisateursComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];

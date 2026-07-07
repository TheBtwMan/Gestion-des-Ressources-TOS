import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

interface NavItem {
  label: string;
  path: string;
  icon: string;
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class ShellComponent {
  readonly reporting: NavItem[] = [{ label: 'Dashboard', path: '/dashboard', icon: '📊' }];

  readonly parametrage: NavItem[] = [
    { label: 'Mode de travail', path: '/parametrage/mode-travail', icon: '🕒' },
    { label: 'Période shift', path: '/parametrage/periode-shift', icon: '⏱' },
    { label: 'Équipes', path: '/parametrage/equipes', icon: '👥' },
    { label: 'Plan de roulement', path: '/parametrage/plan-roulement', icon: '🔁' },
    { label: 'Main théorique', path: '/parametrage/mains-theoriques', icon: '🧮' },
    { label: 'Norme de productivité', path: '/parametrage/normes-productivite', icon: '📐' },
  ];

  readonly exploitation: NavItem[] = [
    { label: 'Escales', path: '/exploitation/escales', icon: '🚢' },
    { label: 'Commandes', path: '/exploitation/commandes', icon: '📦' },
    { label: 'Absences & arrêts', path: '/exploitation/absences-arrets', icon: '⛔' },
  ];

  readonly administration: NavItem[] = [
    { label: 'Profils & droits', path: '/admin/profils', icon: '🔐' },
    { label: 'Utilisateurs', path: '/admin/utilisateurs', icon: '🧑‍💼' },
  ];

  constructor(readonly auth: AuthService) {}

  logout(): void {
    this.auth.logout();
  }

  initiales(): string {
    const name = this.auth.displayName();
    return name
      .split(' ')
      .filter(Boolean)
      .map((p) => p[0]?.toUpperCase())
      .slice(0, 2)
      .join('');
  }
}

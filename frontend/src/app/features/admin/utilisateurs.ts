import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/admin.service';
import { ReferentielService } from '../../core/referentiel.service';
import { AuthService } from '../../core/auth.service';
import { Profil, Terminal, Utilisateur } from '../../core/models';

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './utilisateurs.html',
})
export class UtilisateursComponent implements OnInit {
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly profils = signal<Profil[]>([]);
  readonly terminaux = signal<Terminal[]>([]);
  readonly showCreate = signal(false);

  matricule = '';
  nom = '';
  prenom = '';
  motDePasse = '';
  confirmMotDePasse = '';
  terminalId: number | null = null;
  profilIds = new Set<number>();

  constructor(
    private adminService: AdminService,
    private referentielService: ReferentielService,
    readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
    this.adminService.profils().subscribe((p) => this.profils.set(p));
    this.referentielService.terminaux().subscribe((t) => {
      this.terminaux.set(t);
      if (this.auth.hasDroit('ADMIN_PORT') && !this.auth.hasDroit('GESTION_UTILISATEURS')) {
        this.terminalId = this.auth.session()?.terminalId ?? null;
      } else if (t.length) {
        this.terminalId = t[0].id;
      }
    });
  }

  load(): void {
    this.adminService.utilisateurs().subscribe((u) => {
      if (this.auth.hasDroit('ADMIN_PORT') && !this.auth.hasDroit('GESTION_UTILISATEURS')) {
        const terminalId = this.auth.session()?.terminalId;
        this.utilisateurs.set(u.filter((user) => user.terminal?.id === terminalId));
      } else {
        this.utilisateurs.set(u);
      }
    });
  }

  openCreate(): void {
    this.matricule = '';
    this.nom = '';
    this.prenom = '';
    this.motDePasse = '';
    this.confirmMotDePasse = '';
    this.profilIds = new Set();
    if (this.auth.hasDroit('ADMIN_PORT') && !this.auth.hasDroit('GESTION_UTILISATEURS')) {
      this.terminalId = this.auth.session()?.terminalId ?? null;
    } else {
      const t = this.terminaux();
      if (t.length) this.terminalId = t[0].id;
    }
    this.showCreate.set(true);
  }

  toggleProfil(id: number, checked: boolean): void {
    const set = new Set(this.profilIds);
    if (checked) set.add(id);
    else set.delete(id);
    this.profilIds = set;
  }

  create(): void {
    if (!this.matricule || !this.nom || !this.prenom || !this.motDePasse || this.profilIds.size === 0) return;
    if (this.motDePasse !== this.confirmMotDePasse) return;
    this.adminService
      .createUtilisateur({
        matricule: this.matricule,
        nom: this.nom,
        prenom: this.prenom,
        motDePasse: this.motDePasse,
        terminalId: this.terminalId ?? undefined,
        profilIds: Array.from(this.profilIds),
      })
      .subscribe(() => {
        this.showCreate.set(false);
        this.load();
      });
  }

  desactiver(matricule: string): void {
    this.adminService.desactiverUtilisateur(matricule).subscribe(() => this.load());
  }
}

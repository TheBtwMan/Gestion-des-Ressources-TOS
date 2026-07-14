import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/admin.service';
import { AuthService } from '../../core/auth.service';
import { Droit, Profil } from '../../core/models';

@Component({
  selector: 'app-profils',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './profils.html',
})
export class ProfilsComponent implements OnInit {
  readonly profils = signal<Profil[]>([]);
  readonly droits = signal<Droit[]>([]);
  readonly selected = signal<Profil | null>(null);
  readonly selectedDroits = signal<Set<string>>(new Set());
  readonly showCreate = signal(false);

  nouveauNom = '';

  constructor(private adminService: AdminService, readonly auth: AuthService) {}

  ngOnInit(): void {
    this.load();
    this.adminService.droits().subscribe((d) => this.droits.set(d));
  }

  load(): void {
    this.adminService.profils().subscribe((p) => this.profils.set(p));
  }

  select(profil: Profil): void {
    this.selected.set(profil);
    this.selectedDroits.set(new Set(profil.droits.map((d) => d.code)));
  }

  toggle(code: string, checked: boolean): void {
    const set = new Set(this.selectedDroits());
    if (checked) set.add(code);
    else set.delete(code);
    this.selectedDroits.set(set);
  }

  save(): void {
    const profil = this.selected();
    if (!profil) return;
    const codes = Array.from(this.selectedDroits());
    if (codes.length === 0) return;
    this.adminService.setDroitsProfil(profil.id, codes).subscribe(() => {
      this.load();
      this.selected.set(null);
    });
  }

  createProfil(): void {
    if (!this.nouveauNom) return;
    this.adminService.createProfil({ nom: this.nouveauNom, droits: [] }).subscribe(() => {
      this.nouveauNom = '';
      this.showCreate.set(false);
      this.load();
    });
  }
}

import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParametrageService } from '../../core/parametrage.service';
import { ReferentielService } from '../../core/referentiel.service';
import { Equipe, Personnel, Terminal } from '../../core/models';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-equipes',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './equipes.html',
})
export class EquipesComponent implements OnInit {
  readonly equipes = signal<Equipe[]>([]);
  readonly terminaux = signal<Terminal[]>([]);
  readonly personnel = signal<Personnel[]>([]);
  readonly showCreate = signal(false);
  readonly selectedEquipe = signal<Equipe | null>(null);
  readonly membresSelectionnes = signal<Set<string>>(new Set());

  newEquipe = { id: '', nom: '', responsableMatricule: '', terminalId: 0 };

  constructor(
    private parametrageService: ParametrageService,
    private referentielService: ReferentielService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadEquipes();
    this.referentielService.terminaux().subscribe((t) => {
      this.terminaux.set(t);
      if (t.length) this.newEquipe.terminalId = t[0].id;
    });
    this.referentielService.personnel().subscribe((p) => this.personnel.set(p));
  }

  loadEquipes(): void {
    this.parametrageService.equipes().subscribe((e) => this.equipes.set(e));
  }

  effectif(equipeId: string): number {
    return this.personnel().filter((p) => p.equipe?.id === equipeId).length;
  }

  createEquipe(): void {
    if (!this.newEquipe.id || !this.newEquipe.nom) return;
    this.parametrageService
      .createEquipe({
        id: this.newEquipe.id,
        nom: this.newEquipe.nom,
        responsableMatricule: this.newEquipe.responsableMatricule,
        terminal: { id: this.newEquipe.terminalId } as Terminal,
      })
      .subscribe(() => {
        this.showCreate.set(false);
        this.newEquipe = { id: '', nom: '', responsableMatricule: '', terminalId: this.terminaux()[0]?.id ?? 0 };
        this.loadEquipes();
      });
  }

  openMembres(equipe: Equipe): void {
    this.selectedEquipe.set(equipe);
    const current = new Set(this.personnel().filter((p) => p.equipe?.id === equipe.id).map((p) => p.matricule));
    this.membresSelectionnes.set(current);
  }

  closeMembres(): void {
    this.selectedEquipe.set(null);
  }

  toggleMembre(matricule: string, checked: boolean): void {
    const set = new Set(this.membresSelectionnes());
    if (checked) set.add(matricule);
    else set.delete(matricule);
    this.membresSelectionnes.set(set);
  }

  saveMembres(): void {
    const equipe = this.selectedEquipe();
    if (!equipe) return;
    this.parametrageService.setMembresEquipe(equipe.id, Array.from(this.membresSelectionnes())).subscribe(() => {
      this.referentielService.personnel().subscribe((p) => this.personnel.set(p));
      this.closeMembres();
    });
  }

  personnelParFonction(): { fonction: string; items: Personnel[] }[] {
    const groups = new Map<string, Personnel[]>();
    for (const p of this.personnel()) {
      const key = p.fonction.libelle;
      if (!groups.has(key)) groups.set(key, []);
      groups.get(key)!.push(p);
    }
    return Array.from(groups.entries())
      .map(([fonction, items]) => ({ fonction, items }))
      .sort((a, b) => a.fonction.localeCompare(b.fonction));
  }
}

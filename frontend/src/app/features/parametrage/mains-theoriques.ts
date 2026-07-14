import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParametrageService } from '../../core/parametrage.service';
import { ReferentielService } from '../../core/referentiel.service';
import {
  Accessoire,
  Emplacement,
  EquipementFamille,
  Fonction,
  MainTheorique,
  Terminal,
  Trafic,
} from '../../core/models';

interface RhRow {
  fonctionId: number | null;
  nombreTotal: number;
  emplacement: Emplacement;
  maxNombre: number | null;
  maxObligatoire: boolean;
}

interface MaterielRow {
  familleId: number | null;
}

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-mains-theoriques',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './mains-theoriques.html',
})
export class MainsTheoriquesComponent implements OnInit {
  readonly mains = signal<MainTheorique[]>([]);
  readonly terminaux = signal<Terminal[]>([]);
  readonly trafics = signal<Trafic[]>([]);
  readonly fonctions = signal<Fonction[]>([]);
  readonly familles = signal<EquipementFamille[]>([]);
  readonly accessoires = signal<Accessoire[]>([]);
  readonly showCreate = signal(false);

  nom = '';
  traficId: number | null = null;
  terminalId: number | null = null;
  rhRows: RhRow[] = [];
  materielRows: MaterielRow[] = [];
  accessoireIds = new Set<number>();

  constructor(
    private parametrageService: ParametrageService,
    private referentielService: ReferentielService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
    this.referentielService.terminaux().subscribe((t) => {
      this.terminaux.set(t);
      if (t.length) this.terminalId = t[0].id;
    });
    this.referentielService.trafics().subscribe((t) => {
      this.trafics.set(t);
      if (t.length) this.traficId = t[0].id;
    });
    this.referentielService.fonctions().subscribe((f) => this.fonctions.set(f));
    this.referentielService.equipementFamilles().subscribe((f) => this.familles.set(f));
    this.referentielService.accessoires().subscribe((a) => this.accessoires.set(a));
  }

  load(): void {
    this.parametrageService.mainsTheoriques().subscribe((m) => this.mains.set(m));
  }

  openCreate(): void {
    this.nom = '';
    this.rhRows = [{ fonctionId: null, nombreTotal: 1, emplacement: 'QUAI', maxNombre: null, maxObligatoire: false }];
    this.materielRows = [{ familleId: null }];
    this.accessoireIds = new Set();
    this.showCreate.set(true);
  }

  addRhRow(): void {
    this.rhRows = [...this.rhRows, { fonctionId: null, nombreTotal: 1, emplacement: 'QUAI', maxNombre: null, maxObligatoire: false }];
  }
  removeRhRow(i: number): void {
    this.rhRows = this.rhRows.filter((_, idx) => idx !== i);
  }

  addMaterielRow(): void {
    this.materielRows = [...this.materielRows, { familleId: null }];
  }
  removeMaterielRow(i: number): void {
    this.materielRows = this.materielRows.filter((_, idx) => idx !== i);
  }

  toggleAccessoire(id: number, checked: boolean): void {
    const set = new Set(this.accessoireIds);
    if (checked) set.add(id);
    else set.delete(id);
    this.accessoireIds = set;
  }

  save(): void {
    if (!this.nom || !this.traficId || !this.terminalId) return;
    const body = {
      nom: this.nom,
      trafic: { id: this.traficId },
      terminal: { id: this.terminalId },
      ressourcesHumaines: this.rhRows
        .filter((r) => r.fonctionId)
        .map((r) => ({
          fonction: { id: r.fonctionId },
          nombreTotal: r.nombreTotal,
          emplacement: r.emplacement,
          maxNombre: r.maxNombre,
          maxObligatoire: r.maxObligatoire,
        })),
      ressourcesMaterielles: this.materielRows
        .filter((r) => r.familleId)
        .map((r) => ({ famille: { id: r.familleId } })),
      accessoires: Array.from(this.accessoireIds).map((id) => ({ id })),
    };
    this.parametrageService.createMainTheorique(body).subscribe(() => {
      this.showCreate.set(false);
      this.load();
    });
  }

  remove(id?: number): void {
    if (id == null) return;
    this.parametrageService.deleteMainTheorique(id).subscribe(() => this.load());
  }
}

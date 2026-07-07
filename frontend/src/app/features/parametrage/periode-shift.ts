import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParametrageService } from '../../core/parametrage.service';
import { ReferentielService } from '../../core/referentiel.service';
import { PeriodeShift, Terminal } from '../../core/models';

const DEFAULT_PERIODE: Omit<PeriodeShift, 'terminal'> = {
  shift1NormalDebut: '06:45',
  shift1NormalFin: '14:45',
  shift2NormalDebut: '14:45',
  shift2NormalFin: '22:45',
  shift3NormalDebut: '22:45',
  shift3NormalFin: '06:45',
  shift1RamadanDebut: '07:45',
  shift1RamadanFin: '15:45',
  shift2RamadanDebut: '15:45',
  shift2RamadanFin: '23:45',
  shift3RamadanDebut: '23:45',
  shift3RamadanFin: '07:45',
  ramadanDateDebut: null,
  ramadanDateFin: null,
};

@Component({
  selector: 'app-periode-shift',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './periode-shift.html',
})
export class PeriodeShiftComponent implements OnInit {
  readonly terminaux = signal<Terminal[]>([]);
  readonly terminalId = signal<number | null>(null);
  readonly form = signal<Omit<PeriodeShift, 'terminal'>>({ ...DEFAULT_PERIODE });
  readonly saved = signal(false);

  constructor(private referentielService: ReferentielService, private parametrageService: ParametrageService) {}

  ngOnInit(): void {
    this.referentielService.terminaux().subscribe((terminaux) => {
      this.terminaux.set(terminaux);
      if (terminaux.length) {
        this.terminalId.set(terminaux[0].id);
        this.load(terminaux[0].id);
      }
    });
  }

  onTerminalChange(id: string): void {
    const terminalId = Number(id);
    this.terminalId.set(terminalId);
    this.load(terminalId);
  }

  load(terminalId: number): void {
    this.parametrageService.getPeriodeShift(terminalId).subscribe({
      next: (p) => this.form.set({ ...p }),
      error: () => this.form.set({ ...DEFAULT_PERIODE }),
    });
  }

  update<K extends keyof Omit<PeriodeShift, 'terminal'>>(key: K, value: PeriodeShift[K]): void {
    this.form.set({ ...this.form(), [key]: value });
  }

  save(): void {
    const terminalId = this.terminalId();
    if (!terminalId) return;
    this.saved.set(false);
    this.parametrageService
      .savePeriodeShift(terminalId, { ...this.form(), terminal: { id: terminalId } as Terminal })
      .subscribe(() => this.saved.set(true));
  }
}

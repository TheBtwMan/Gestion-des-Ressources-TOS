import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParametrageService } from '../../core/parametrage.service';
import { ReferentielService } from '../../core/referentiel.service';
import { JourType, ModeTravail, SemaineType, Terminal } from '../../core/models';

@Component({
  selector: 'app-mode-travail',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './mode-travail.html',
})
export class ModeTravailComponent implements OnInit {
  readonly terminaux = signal<Terminal[]>([]);
  readonly terminalId = signal<number | null>(null);
  readonly semaine = signal<SemaineType>('SIX_SUR_SEPT');
  readonly jour = signal<JourType>('DEUX_SHIFTS');
  readonly saved = signal(false);
  readonly loading = signal(false);

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
    this.loading.set(true);
    this.parametrageService.getModeTravail(terminalId).subscribe({
      next: (mode: ModeTravail) => {
        this.semaine.set(mode.semaine);
        this.jour.set(mode.jour);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  save(): void {
    const terminalId = this.terminalId();
    if (!terminalId) return;
    this.saved.set(false);
    this.parametrageService
      .saveModeTravail(terminalId, {
        terminal: { id: terminalId } as Terminal,
        semaine: this.semaine(),
        jour: this.jour(),
      })
      .subscribe(() => this.saved.set(true));
  }
}

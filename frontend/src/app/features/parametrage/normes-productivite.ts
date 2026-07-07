import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParametrageService } from '../../core/parametrage.service';
import { ReferentielService } from '../../core/referentiel.service';
import { MainTheorique, NatureSuivi, NormeProductivite, Trafic } from '../../core/models';

@Component({
  selector: 'app-normes-productivite',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './normes-productivite.html',
})
export class NormesProductiviteComponent implements OnInit {
  readonly normes = signal<NormeProductivite[]>([]);
  readonly trafics = signal<Trafic[]>([]);
  readonly mains = signal<MainTheorique[]>([]);

  traficId: number | null = null;
  mainId: number | null = null;
  mode = 'T/Shift';
  norme = 1000;
  natureSuivi: NatureSuivi = 'SHIFT';
  import = true;
  export = false;

  readonly mainsDuTrafic = computed(() => this.mains().filter((m) => m.trafic.id === this.traficId));

  constructor(private parametrageService: ParametrageService, private referentielService: ReferentielService) {}

  ngOnInit(): void {
    this.load();
    this.referentielService.trafics().subscribe((t) => {
      this.trafics.set(t);
      if (t.length) this.traficId = t[0].id;
    });
    this.parametrageService.mainsTheoriques().subscribe((m) => {
      this.mains.set(m);
      if (m.length) this.mainId = m[0].id ?? null;
    });
  }

  load(): void {
    this.parametrageService.normesProductivite().subscribe((n) => this.normes.set(n));
  }

  create(): void {
    if (!this.traficId || !this.mainId || (!this.import && !this.export)) return;
    const sensList = [...(this.import ? ['IMPORT'] : []), ...(this.export ? ['EXPORT'] : [])];
    let pending = sensList.length;
    for (const sens of sensList) {
      this.parametrageService
        .createNorme({
          trafic: { id: this.traficId },
          mainTheorique: { id: this.mainId },
          mode: this.mode,
          norme: this.norme,
          sens,
          natureSuivi: this.natureSuivi,
        })
        .subscribe(() => {
          pending -= 1;
          if (pending === 0) this.load();
        });
    }
  }

  remove(id?: number): void {
    if (id == null) return;
    this.parametrageService.deleteNorme(id).subscribe(() => this.load());
  }
}

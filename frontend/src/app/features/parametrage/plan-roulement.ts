import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ParametrageService } from '../../core/parametrage.service';
import { Equipe, PlanRoulement, ShiftValue, TypeRoulement } from '../../core/models';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-plan-roulement',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './plan-roulement.html',
})
export class PlanRoulementComponent implements OnInit {
  readonly equipes = signal<Equipe[]>([]);
  readonly plans = signal<PlanRoulement[]>([]);

  typeRoulement: TypeRoulement = 'SEMAINE';
  dateDebut = '';
  dateFin = '';
  equipeId = '';
  shift: ShiftValue = 'SHIFT_1';

  constructor(private parametrageService: ParametrageService, public auth: AuthService) {}

  ngOnInit(): void {
    this.parametrageService.equipes().subscribe((e) => {
      this.equipes.set(e);
      if (e.length) this.equipeId = e[0].id;
    });
    this.load();
  }

  load(): void {
    this.parametrageService.planRoulement().subscribe((p) => this.plans.set(p));
  }

  create(): void {
    if (!this.equipeId || !this.dateDebut || !this.dateFin) return;
    this.parametrageService
      .createPlanRoulement({
        typeRoulement: this.typeRoulement,
        dateDebut: this.dateDebut,
        dateFin: this.dateFin,
        equipe: { id: this.equipeId },
        shift: this.shift,
      })
      .subscribe(() => this.load());
  }

  remove(id?: number): void {
    if (id == null) return;
    this.parametrageService.deletePlanRoulement(id).subscribe(() => this.load());
  }

  shiftLabel(s: ShiftValue): string {
    return s.replace('SHIFT_', 'Shift ');
  }
}

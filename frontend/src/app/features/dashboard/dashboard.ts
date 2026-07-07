import { DecimalPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { DashboardService } from '../../core/dashboard.service';
import { AbsenteismeParEquipe, ArretParEquipement, DashboardKpis, TonnageParTrafic } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit {
  readonly kpis = signal<DashboardKpis | null>(null);
  readonly tonnageParTrafic = signal<TonnageParTrafic[]>([]);
  readonly absenteisme = signal<AbsenteismeParEquipe[]>([]);
  readonly arretsParEquipement = signal<ArretParEquipement[]>([]);
  readonly loading = signal(true);

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.dashboardService.kpis().subscribe((v) => this.kpis.set(v));
    this.dashboardService.tonnageParTrafic().subscribe((v) => this.tonnageParTrafic.set(v));
    this.dashboardService.absenteismeParEquipe().subscribe((v) => this.absenteisme.set(v));
    this.dashboardService.arretsParEquipement().subscribe((v) => {
      this.arretsParEquipement.set(v);
      this.loading.set(false);
    });
  }

  maxTonnage(): number {
    const list = this.tonnageParTrafic();
    return Math.max(1, ...list.map((t) => Math.max(t.tonnagePrevu, t.tonnageRealise)));
  }

  barWidth(value: number): number {
    return Math.min(100, Math.round((value / this.maxTonnage()) * 100));
  }

  maxAbsences(): number {
    return Math.max(1, ...this.absenteisme().map((a) => a.nbAbsences));
  }

  absenceWidth(value: number): number {
    return Math.min(100, Math.round((value / this.maxAbsences()) * 100));
  }

  maxDuree(): number {
    return Math.max(1, ...this.arretsParEquipement().map((a) => a.dureeTotaleMinutes));
  }

  dureeWidth(value: number): number {
    return Math.min(100, Math.round((value / this.maxDuree()) * 100));
  }

  formatMinutes(minutes: number): string {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return h > 0 ? `${h}h${m.toString().padStart(2, '0')}` : `${m}min`;
  }

  statutBadgeClass(statut: string): string {
    switch (statut) {
      case 'EN_COURS': return 'badge badge-orange';
      case 'CLOTUREE': return 'badge badge-green';
      default: return 'badge badge-blue';
    }
  }

  statutLabel(statut: string): string {
    switch (statut) {
      case 'EN_COURS': return 'En cours';
      case 'CLOTUREE': return 'Clôturée';
      default: return 'Prévue';
    }
  }

  statutEntries(): [string, number][] {
    const map = this.kpis()?.escalesParStatut ?? ({} as Record<string, number>);
    return Object.entries(map);
  }
}

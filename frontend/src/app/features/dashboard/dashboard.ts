import { DecimalPipe } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardFilters, DashboardService } from '../../core/dashboard.service';
import { ReferentielService } from '../../core/referentiel.service';
import { AbsenteismeParEquipe, ArretParEquipement, DashboardKpis, Terminal, TonnageParTrafic } from '../../core/models';

const AUTO_REFRESH_INTERVAL_MS = 20000;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  readonly kpis = signal<DashboardKpis | null>(null);
  readonly tonnageParTrafic = signal<TonnageParTrafic[]>([]);
  readonly absenteisme = signal<AbsenteismeParEquipe[]>([]);
  readonly arretsParEquipement = signal<ArretParEquipement[]>([]);
  readonly loading = signal(true);

  readonly terminaux = signal<Terminal[]>([]);
  readonly terminalId = signal<number | null>(null);
  readonly dateDebut = signal<string>('');
  readonly dateFin = signal<string>('');

  readonly autoRefresh = signal(true);
  readonly lastUpdated = signal<Date | null>(null);
  readonly exporting = signal(false);

  private intervalHandle?: ReturnType<typeof setInterval>;

  constructor(
    private dashboardService: DashboardService,
    private referentielService: ReferentielService,
  ) {}

  ngOnInit(): void {
    this.referentielService.terminaux().subscribe((v) => this.terminaux.set(v));
    this.refresh();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.stopAutoRefresh();
  }

  private currentFilters(): DashboardFilters {
    return {
      terminalId: this.terminalId(),
      dateDebut: this.dateDebut() || null,
      dateFin: this.dateFin() || null,
    };
  }

  onFiltersChange(): void {
    this.refresh();
  }

  resetFilters(): void {
    this.terminalId.set(null);
    this.dateDebut.set('');
    this.dateFin.set('');
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    const filters = this.currentFilters();
    this.dashboardService.kpis(filters).subscribe((v) => this.kpis.set(v));
    this.dashboardService.tonnageParTrafic(filters).subscribe((v) => this.tonnageParTrafic.set(v));
    this.dashboardService.absenteismeParEquipe(filters).subscribe((v) => this.absenteisme.set(v));
    this.dashboardService.arretsParEquipement(filters).subscribe((v) => {
      this.arretsParEquipement.set(v);
      this.loading.set(false);
      this.lastUpdated.set(new Date());
    });
  }

  toggleAutoRefresh(): void {
    this.autoRefresh.set(!this.autoRefresh());
    if (this.autoRefresh()) {
      this.startAutoRefresh();
    } else {
      this.stopAutoRefresh();
    }
  }

  private startAutoRefresh(): void {
    this.stopAutoRefresh();
    if (this.autoRefresh()) {
      this.intervalHandle = setInterval(() => this.refresh(), AUTO_REFRESH_INTERVAL_MS);
    }
  }

  private stopAutoRefresh(): void {
    if (this.intervalHandle) {
      clearInterval(this.intervalHandle);
      this.intervalHandle = undefined;
    }
  }

  exportCsv(type: 'commandes' | 'kpis'): void {
    this.exporting.set(true);
    const filters = this.currentFilters();
    const obs = type === 'commandes'
      ? this.dashboardService.exportCommandesCsv(filters)
      : this.dashboardService.exportKpisCsv(filters);

    obs.subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = type === 'commandes' ? 'commandes.csv' : 'dashboard-kpis.csv';
        a.click();
        URL.revokeObjectURL(url);
        this.exporting.set(false);
      },
      error: () => this.exporting.set(false),
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

  formatTime(date: Date | null): string {
    return date ? date.toLocaleTimeString('fr-FR') : '—';
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

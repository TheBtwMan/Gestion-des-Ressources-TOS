import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { AbsenteismeParEquipe, ArretParEquipement, DashboardKpis, Escale, TonnageParTrafic } from './models';

const base = `${environment.apiUrl}/dashboard`;

export interface DashboardFilters {
  terminalId?: number | null;
  dateDebut?: string | null; // yyyy-MM-dd
  dateFin?: string | null; // yyyy-MM-dd
}

function toParams(filters?: DashboardFilters): HttpParams {
  let params = new HttpParams();
  if (filters?.terminalId != null) {
    params = params.set('terminalId', filters.terminalId);
  }
  if (filters?.dateDebut) {
    params = params.set('dateDebut', filters.dateDebut);
  }
  if (filters?.dateFin) {
    params = params.set('dateFin', filters.dateFin);
  }
  return params;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  kpis(filters?: DashboardFilters) {
    return this.http.get<DashboardKpis>(`${base}/kpis`, { params: toParams(filters) });
  }
  tonnageParTrafic(filters?: DashboardFilters) {
    return this.http.get<TonnageParTrafic[]>(`${base}/tonnage-par-trafic`, { params: toParams(filters) });
  }
  absenteismeParEquipe(filters?: DashboardFilters) {
    return this.http.get<AbsenteismeParEquipe[]>(`${base}/absenteisme-par-equipe`, { params: toParams(filters) });
  }
  arretsParEquipement(filters?: DashboardFilters) {
    return this.http.get<ArretParEquipement[]>(`${base}/arrets-par-equipement`, { params: toParams(filters) });
  }
  escalesEnCours() {
    return this.http.get<Escale[]>(`${base}/escales-en-cours`);
  }
  exportCommandesCsv(filters?: DashboardFilters) {
    return this.http.get(`${base}/export/commandes.csv`, { params: toParams(filters), responseType: 'blob' });
  }
  exportKpisCsv(filters?: DashboardFilters) {
    return this.http.get(`${base}/export/kpis.csv`, { params: toParams(filters), responseType: 'blob' });
  }
}

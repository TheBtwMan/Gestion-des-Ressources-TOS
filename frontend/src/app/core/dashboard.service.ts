import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { AbsenteismeParEquipe, ArretParEquipement, DashboardKpis, Escale, TonnageParTrafic } from './models';

const base = `${environment.apiUrl}/dashboard`;

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  kpis() { return this.http.get<DashboardKpis>(`${base}/kpis`); }
  tonnageParTrafic() { return this.http.get<TonnageParTrafic[]>(`${base}/tonnage-par-trafic`); }
  absenteismeParEquipe() { return this.http.get<AbsenteismeParEquipe[]>(`${base}/absenteisme-par-equipe`); }
  arretsParEquipement() { return this.http.get<ArretParEquipement[]>(`${base}/arrets-par-equipement`); }
  escalesEnCours() { return this.http.get<Escale[]>(`${base}/escales-en-cours`); }
}

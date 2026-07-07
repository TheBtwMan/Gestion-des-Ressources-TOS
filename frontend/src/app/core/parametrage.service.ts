import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Equipe, MainTheorique, ModeTravail, NormeProductivite, PeriodeShift, Personnel, PlanRoulement } from './models';

const base = `${environment.apiUrl}/parametrage`;

@Injectable({ providedIn: 'root' })
export class ParametrageService {
  constructor(private http: HttpClient) {}

  getModeTravail(terminalId: number) { return this.http.get<ModeTravail>(`${base}/mode-travail/terminal/${terminalId}`); }
  saveModeTravail(terminalId: number, body: Partial<ModeTravail>) { return this.http.put<ModeTravail>(`${base}/mode-travail/terminal/${terminalId}`, body); }

  getPeriodeShift(terminalId: number) { return this.http.get<PeriodeShift>(`${base}/periode-shift/terminal/${terminalId}`); }
  savePeriodeShift(terminalId: number, body: Partial<PeriodeShift>) { return this.http.put<PeriodeShift>(`${base}/periode-shift/terminal/${terminalId}`, body); }

  equipes(terminalId?: number) {
    const url = terminalId ? `${base}/equipes?terminalId=${terminalId}` : `${base}/equipes`;
    return this.http.get<Equipe[]>(url);
  }
  membresEquipe(equipeId: string) { return this.http.get<Personnel[]>(`${base}/equipes/${equipeId}/membres`); }
  createEquipe(body: Partial<Equipe>) { return this.http.post<Equipe>(`${base}/equipes`, body); }
  setMembresEquipe(equipeId: string, matricules: string[]) { return this.http.put<Personnel[]>(`${base}/equipes/${equipeId}/membres`, matricules); }
  deleteEquipe(id: string) { return this.http.delete<void>(`${base}/equipes/${id}`); }

  planRoulement(equipeId?: string) {
    const url = equipeId ? `${base}/plan-roulement?equipeId=${equipeId}` : `${base}/plan-roulement`;
    return this.http.get<PlanRoulement[]>(url);
  }
  createPlanRoulement(body: any) { return this.http.post<PlanRoulement>(`${base}/plan-roulement`, body); }
  deletePlanRoulement(id: number) { return this.http.delete<void>(`${base}/plan-roulement/${id}`); }

  mainsTheoriques(params?: { traficId?: number; terminalId?: number }) {
    const qs = params ? Object.entries(params).filter(([, v]) => v != null).map(([k, v]) => `${k}=${v}`).join('&') : '';
    return this.http.get<MainTheorique[]>(`${base}/mains-theoriques${qs ? '?' + qs : ''}`);
  }
  getMainTheorique(id: number) { return this.http.get<MainTheorique>(`${base}/mains-theoriques/${id}`); }
  createMainTheorique(body: any) { return this.http.post<MainTheorique>(`${base}/mains-theoriques`, body); }
  updateMainTheorique(id: number, body: any) { return this.http.put<MainTheorique>(`${base}/mains-theoriques/${id}`, body); }
  deleteMainTheorique(id: number) { return this.http.delete<void>(`${base}/mains-theoriques/${id}`); }

  normesProductivite(traficId?: number) {
    const url = traficId ? `${base}/normes-productivite?traficId=${traficId}` : `${base}/normes-productivite`;
    return this.http.get<NormeProductivite[]>(url);
  }
  createNorme(body: any) { return this.http.post<NormeProductivite>(`${base}/normes-productivite`, body); }
  deleteNorme(id: number) { return this.http.delete<void>(`${base}/normes-productivite/${id}`); }
}

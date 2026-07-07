import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Droit, Profil, Utilisateur } from './models';

const base = `${environment.apiUrl}/admin`;

export interface UtilisateurRequest {
  matricule?: string;
  nom: string;
  prenom: string;
  motDePasse?: string;
  terminalId?: number;
  profilIds: number[];
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private http: HttpClient) {}

  droits() { return this.http.get<Droit[]>(`${base}/droits`); }
  profils() { return this.http.get<Profil[]>(`${base}/profils`); }
  createProfil(body: { nom: string; droits: { code: string }[] }) { return this.http.post<Profil>(`${base}/profils`, body); }
  setDroitsProfil(id: number, droitCodes: string[]) { return this.http.put<Profil>(`${base}/profils/${id}/droits`, droitCodes); }

  utilisateurs() { return this.http.get<Utilisateur[]>(`${base}/utilisateurs`); }
  createUtilisateur(body: UtilisateurRequest) { return this.http.post<Utilisateur>(`${base}/utilisateurs`, body); }
  updateUtilisateur(matricule: string, body: UtilisateurRequest) { return this.http.put<Utilisateur>(`${base}/utilisateurs/${matricule}`, body); }
  desactiverUtilisateur(matricule: string) { return this.http.delete<Utilisateur>(`${base}/utilisateurs/${matricule}`); }
}

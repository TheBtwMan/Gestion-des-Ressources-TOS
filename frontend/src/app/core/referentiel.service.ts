import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Accessoire, Equipement, EquipementFamille, Fonction, Personnel, Port, Terminal, Trafic, TypeTrafic } from './models';

const base = `${environment.apiUrl}/referentiel`;

@Injectable({ providedIn: 'root' })
export class ReferentielService {
  constructor(private http: HttpClient) {}

  ports() { return this.http.get<Port[]>(`${base}/ports`); }
  terminaux() { return this.http.get<Terminal[]>(`${base}/terminaux`); }
  fonctions() { return this.http.get<Fonction[]>(`${base}/fonctions`); }
  personnel() { return this.http.get<Personnel[]>(`${base}/personnel`); }
  personnelParEquipe(equipeId: string) { return this.http.get<Personnel[]>(`${base}/personnel/equipe/${equipeId}`); }
  personnelParFonction(fonctionCode: string) { return this.http.get<Personnel[]>(`${base}/personnel/fonction/${fonctionCode}`); }
  equipementFamilles() { return this.http.get<EquipementFamille[]>(`${base}/equipement-familles`); }
  equipements() { return this.http.get<Equipement[]>(`${base}/equipements`); }
  equipementsParFamille(familleId: number) { return this.http.get<Equipement[]>(`${base}/equipements/famille/${familleId}`); }
  accessoires() { return this.http.get<Accessoire[]>(`${base}/accessoires`); }
  typeTrafics() { return this.http.get<TypeTrafic[]>(`${base}/type-trafics`); }
  trafics() { return this.http.get<Trafic[]>(`${base}/trafics`); }
}

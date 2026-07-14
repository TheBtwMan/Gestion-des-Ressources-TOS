// Modèles miroir des entités backend (opération Manutention, module "Gestion des ressources").

export interface Port {
  id: number;
  nom: string;
}

export interface Terminal {
  id: number;
  nom: string;
  port: Port;
}

export interface Fonction {
  id: number;
  code: string;
  libelle: string;
  vacationAutorisee: boolean;
}

export interface Personnel {
  matricule: string;
  nom: string;
  fonction: Fonction;
  equipe?: Equipe | null;
  actif: boolean;
}

export interface EquipementFamille {
  id: number;
  nom: string;
}

export interface Equipement {
  code: string;
  famille: EquipementFamille;
  disponible: boolean;
}

export interface Accessoire {
  id: number;
  nom: string;
}

export interface TypeTrafic {
  id: number;
  nom: string;
}

export interface Trafic {
  id: number;
  nom: string;
  code: string;
  typeTrafic: TypeTrafic;
}

export type SemaineType = 'SIX_SUR_SEPT' | 'SEPT_SUR_SEPT';
export type JourType = 'DEUX_SHIFTS' | 'TROIS_SHIFTS';
export type ShiftValue = 'SHIFT_1' | 'SHIFT_2' | 'SHIFT_3';
export type Emplacement = 'BORD' | 'QUAI' | 'ARRIERE';
export type Vacation = 'VACATION_1' | 'VACATION_2';
export type Sens = 'IMPORT' | 'EXPORT';
export type NatureSuivi = 'SHIFT' | 'FIN_DU_TRAVAIL';
export type TypeRoulement = 'MOIS' | 'SEMAINE';
export type StatutEscale = 'PREVU' | 'EN_COURS' | 'CLOTUREE';
export type StatutCommande = 'CREEE' | 'LIEE_ESCALE' | 'EN_COURS' | 'VALIDEE';

export interface ModeTravail {
  id?: number;
  terminal: Terminal;
  semaine: SemaineType;
  jour: JourType;
}

export interface PeriodeShift {
  id?: number;
  terminal: Terminal;
  shift1NormalDebut: string;
  shift1NormalFin: string;
  shift2NormalDebut: string;
  shift2NormalFin: string;
  shift3NormalDebut: string;
  shift3NormalFin: string;
  shift1RamadanDebut: string;
  shift1RamadanFin: string;
  shift2RamadanDebut: string;
  shift2RamadanFin: string;
  shift3RamadanDebut: string;
  shift3RamadanFin: string;
  ramadanDateDebut?: string | null;
  ramadanDateFin?: string | null;
}

export interface Equipe {
  id: string;
  nom: string;
  responsableMatricule?: string;
  terminal: Terminal;
}

export interface PlanRoulement {
  id?: number;
  typeRoulement: TypeRoulement;
  dateDebut: string;
  dateFin: string;
  equipe: Equipe;
  shift: ShiftValue;
}

export interface MainRessourceHumaine {
  id?: number;
  fonction: Fonction;
  nombreTotal: number;
  emplacement: Emplacement;
  maxNombre?: number | null;
  maxObligatoire: boolean;
}

export interface MainRessourceMaterielle {
  id?: number;
  famille: EquipementFamille;
}

export interface MainTheorique {
  id?: number;
  nom: string;
  trafic: Trafic;
  terminal: Terminal;
  ressourcesHumaines: MainRessourceHumaine[];
  ressourcesMaterielles: MainRessourceMaterielle[];
  accessoires: Accessoire[];
}

export interface NormeProductivite {
  id?: number;
  trafic: Trafic;
  mainTheorique: MainTheorique;
  mode: string;
  norme: number;
  sens: Sens;
  natureSuivi: NatureSuivi;
}

export interface Escale {
  id: string;
  navire: string;
  compagnie?: string;
  terminal: Terminal;
  dateArriveePrevue: string;
  dateArriveeReelle?: string | null;
  dateDepartPrevue: string;
  dateDebutTravail?: string | null;
  dateFinTravail?: string | null;
  statut: StatutEscale;
}

export interface CommandeShiftTonnage {
  id?: number;
  shiftId: string;
  tonnageRealise?: number | null;
}

export interface Commande {
  numero: string;
  escale?: Escale | null;
  client?: string;
  sens: Sens;
  trafic: Trafic;
  mainTheorique?: MainTheorique | null;
  marchandise?: string;
  dateTravail: string;
  shift: ShiftValue;
  tonnagePrevu: number;
  tonnageRealiseParShift: boolean;
  statut: StatutCommande;
  shifts?: CommandeShiftTonnage[];
}

export interface AffectationPrevisionnelleRH {
  id?: number;
  equipe?: Equipe | null;
  fonction: Fonction;
  personnel: Personnel;
  emplacement: Emplacement;
  vacation?: Vacation | null;
  appelExterne: boolean;
}

export interface AffectationPrevisionnelleMateriel {
  id?: number;
  equipement: Equipement;
  appelExterne: boolean;
}

export interface AffectationPrevisionnelleAccessoire {
  id?: number;
  accessoire: Accessoire;
  appelExterne: boolean;
}

export interface AffectationPrevisionnelle {
  id?: number;
  commande: Commande;
  mainTheorique: MainTheorique;
  dateTravail: string;
  shift: ShiftValue;
  ressourcesHumaines: AffectationPrevisionnelleRH[];
  ressourcesMaterielles: AffectationPrevisionnelleMateriel[];
  accessoires: AffectationPrevisionnelleAccessoire[];
}

export interface AffectationReelleRH {
  id?: number;
  personnel: Personnel;
  fonction: Fonction;
  emplacement: Emplacement;
  vacation?: Vacation | null;
  dateDebut?: string | null;
  dateFin?: string | null;
  horsPrevisionnel: boolean;
}

export interface AffectationReelleMateriel {
  id?: number;
  equipement: Equipement;
  dateDebut?: string | null;
  dateFin?: string | null;
}

export interface AffectationReelleAccessoire {
  id?: number;
  accessoire: Accessoire;
  dateDebut?: string | null;
  dateFin?: string | null;
}

export interface AffectationReelle {
  id?: number;
  commande: Commande;
  mainTheorique?: MainTheorique | null;
  dateTravail: string;
  shift: ShiftValue;
  tonnageRealise?: number | null;
  cdi?: number | null;
  cdd?: number | null;
  sousTraitant?: number | null;
  ressourcesHumaines: AffectationReelleRH[];
  ressourcesMaterielles: AffectationReelleMateriel[];
  accessoires: AffectationReelleAccessoire[];
}

export interface Arret {
  id?: string;
  equipement?: Equipement | null;
  escale?: Escale | null;
  codeArret?: string;
  description?: string;
  dateDebut?: string | null;
  dateFin?: string | null;
  dureeMinutes?: number | null;
}

export interface Absence {
  id?: string;
  personnel: Personnel;
  escale?: Escale | null;
  dateDebut?: string | null;
  dateFin?: string | null;
  shift?: ShiftValue | null;
  motif: string;
}

export interface Droit {
  code: string;
  libelle: string;
}

export interface Profil {
  id: number;
  nom: string;
  droits: Droit[];
}

export interface Utilisateur {
  matricule: string;
  nom: string;
  prenom: string;
  terminal?: Terminal | null;
  profils: Profil[];
  actif: boolean;
}

export interface LoginResponse {
  token: string;
  matricule: string;
  nom: string;
  prenom: string;
  profils: string[];
  droits: string[];
  terminalId?: number | null;
  terminalNom?: string | null;
}

export interface DashboardKpis {
  tonnagePrevu: number;
  tonnageRealise: number;
  ecartPourcent: number;
  nbEscales: number;
  escalesParStatut: Record<StatutEscale, number>;
  nbCommandes: number;
  nbArrets: number;
  arretsEnCours: number;
  dureeArretTotaleMinutes: number;
  nbAbsences: number;
  effectifTotal: number;
  tauxAbsenteismePourcent: number;
}

export interface TonnageParTrafic {
  trafic: string;
  tonnagePrevu: number;
  tonnageRealise: number;
  ecartPourcent: number;
}

export interface AbsenteismeParEquipe {
  equipeId: string;
  effectif: number;
  nbAbsences: number;
}

export interface ArretParEquipement {
  equipementCode: string;
  famille: string;
  nbArrets: number;
  dureeTotaleMinutes: number;
}

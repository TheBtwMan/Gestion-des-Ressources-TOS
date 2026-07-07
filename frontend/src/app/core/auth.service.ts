import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse } from './models';

const STORAGE_KEY = 'mm_session';

interface Session {
  token: string;
  matricule: string;
  nom: string;
  prenom: string;
  profils: string[];
  droits: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sessionSignal = signal<Session | null>(this.readStoredSession());

  readonly session = this.sessionSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.sessionSignal() !== null);
  readonly displayName = computed(() => {
    const s = this.sessionSignal();
    return s ? `${s.prenom} ${s.nom}` : '';
  });

  constructor(private http: HttpClient, private router: Router) {}

  login(matricule: string, motDePasse: string) {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { matricule, motDePasse })
      .pipe(
        tap((res) => {
          const session: Session = {
            token: res.token,
            matricule: res.matricule,
            nom: res.nom,
            prenom: res.prenom,
            profils: res.profils,
            droits: res.droits,
          };
          this.sessionSignal.set(session);
          localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
        })
      );
  }

  logout() {
    this.sessionSignal.set(null);
    localStorage.removeItem(STORAGE_KEY);
    this.router.navigateByUrl('/login');
  }

  getToken(): string | null {
    return this.sessionSignal()?.token ?? null;
  }

  hasDroit(code: string): boolean {
    return this.sessionSignal()?.droits.includes(code) ?? false;
  }

  private readStoredSession(): Session | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as Session;
    } catch {
      return null;
    }
  }
}

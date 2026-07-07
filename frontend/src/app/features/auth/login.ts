import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  matricule = '';
  motDePasse = '';
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    if (!this.matricule || !this.motDePasse) {
      this.error.set('Veuillez renseigner votre matricule et votre mot de passe.');
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(this.matricule, this.motDePasse).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Matricule ou mot de passe incorrect.');
      },
    });
  }
}

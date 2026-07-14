import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) {
    return true;
  }
  router.navigateByUrl('/login');
  return false;
};

/**
 * Fabrique de guard qui vérifie si l'utilisateur connecté possède au moins
 * un des droits listés. Si aucun droit ne correspond, redirige vers /dashboard.
 */
export function droitGuard(...droits: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.isAuthenticated()) {
      router.navigateByUrl('/login');
      return false;
    }
    const hasAny = droits.some((d) => auth.hasDroit(d));
    if (!hasAny) {
      router.navigateByUrl('/dashboard');
      return false;
    }
    return true;
  };
}

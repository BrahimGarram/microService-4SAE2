import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError, take } from 'rxjs/operators';
import { TestPassComponent } from '../components/test-pass/test-pass.component';
import { AffectationTestService } from '../services/affectation-test.service';

/**
 * Guard qui intercepte la sortie de la page "test/pass".
 * Si le freelancer quitte sans avoir soumis, on appelle l'API abandon
 * et on attend la réponse avant d'autoriser la navigation (navbar, lien, etc.).
 * Ainsi le statut passe bien à "Abandonné" en base.
 */
@Injectable({
  providedIn: 'root'
})
export class TestPassDeactivateGuard implements CanDeactivate<TestPassComponent> {

  constructor(private affectationService: AffectationTestService) {}

  canDeactivate(
    component: TestPassComponent,
  ): Observable<boolean> {
    const needAbandon = !component.submitted
      && component.affectationId != null
      && component.questions.length > 0;

    if (!needAbandon) {
      return of(true);
    }

    return this.affectationService.abandonTest(component.affectationId!).pipe(
      take(1),
      map(() => true),
      catchError(() => of(true)) // autoriser la navigation même si l'API échoue
    );
  }
}

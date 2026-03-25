import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap, tap } from 'rxjs/operators';
import { AlertService } from '../../core/services/alert.service';
import { NotificationService } from '../../core/services/notification.service';
import { AlertActions } from './alerts.actions';

@Injectable()
export class AlertEffects {
  private readonly actions$ = inject(Actions);
  private readonly alertService = inject(AlertService);
  private readonly notificationService = inject(NotificationService);

  readonly loadAlerts$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AlertActions.loadAlerts),
      switchMap(({ status, severity }) =>
        this.alertService.getAll(status, severity).pipe(
          map((alerts) => AlertActions.loadAlertsSuccess({ alerts })),
          catchError((error: { message?: string }) =>
            of(AlertActions.loadAlertsFailure({ error: error.message ?? 'Failed to load alerts' })),
          ),
        ),
      ),
    ),
  );

  readonly loadStats$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AlertActions.loadAlertStats),
      switchMap(() =>
        this.alertService.getStats().pipe(
          map((stats) => AlertActions.loadAlertStatsSuccess({ stats })),
          catchError((error: { message?: string }) =>
            of(AlertActions.loadAlertStatsFailure({ error: error.message ?? 'Failed to load alert stats' })),
          ),
        ),
      ),
    ),
  );

  readonly updateStatus$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AlertActions.updateAlertStatus),
      mergeMap(({ id, status }) =>
        this.alertService.updateStatus(id, status).pipe(
          map((alert) => AlertActions.updateAlertStatusSuccess({ alert })),
          catchError((error: { message?: string }) =>
            of(AlertActions.updateAlertStatusFailure({ error: error.message ?? 'Failed to update alert status' })),
          ),
        ),
      ),
    ),
  );

  readonly notifyOnCriticalAlerts$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AlertActions.loadAlertsSuccess),
        tap(({ alerts }) => {
          const hasCritical = alerts.some(
            (alert) => alert.severity === 'CRITICAL' && (alert.status === 'NEW' || alert.status === 'PENDING'),
          );

          if (hasCritical) {
            this.notificationService.notify('CRITICAL Security Alert', {
              body: 'A critical security threat was detected. Open the dashboard now to take action.',
            });
          }
        }),
      ),
    { dispatch: false },
  );
}

import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { AgentService } from '../../../core/services/agent.service';
import { AlertService } from '../../../core/services/alert.service';
import { OverviewActions } from './overview.actions';
import { catchError, map, switchMap } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';

@Injectable()
export class OverviewEffects {
  private readonly actions$ = inject(Actions);
  private readonly agentService = inject(AgentService);
  private readonly alertService = inject(AlertService);

  readonly loadDashboard$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OverviewActions.loadDashboard),
      switchMap(() =>
        forkJoin({
          agentStats: this.agentService.getStats(),
          alerts: this.alertService.getAll(),
        }).pipe(
          map(({ agentStats, alerts }) => {
            const unitSummary = [
              { label: 'Active Agents', value: agentStats.active.toString(), icon: 'smart_toy', hint: null },
              {
                label: 'Total Offline',
                value: (agentStats.inactive + agentStats.error + agentStats.revoked).toString(),
                icon: 'power_off',
                hint: null,
              },
            ];

            const performance = [
              {
                label: 'Total Deployed',
                value: (agentStats.active + agentStats.inactive + agentStats.error + agentStats.revoked).toString(),
                icon: 'memory',
                hint: 'Fleet size',
              },
              {
                label: 'Critical Errors',
                value: agentStats.error.toString(),
                icon: 'warning',
                hint: agentStats.error > 0 ? 'Action needed' : 'Nominal',
              },
              { label: 'Revoked', value: agentStats.revoked.toString(), icon: 'block', hint: 'Keys revoked' },
              { label: 'Network', value: 'OK', icon: 'wifi', hint: 'Stable link' },
            ];

            const logs = alerts.slice(0, 5).map((alert) => ({
              message: alert.description,
              timestamp: new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
              category: alert.threatType,
              highlight: alert.severity === 'CRITICAL' || alert.severity === 'HIGH',
            }));

            return OverviewActions.loadDashboardSuccess({ unitSummary, performance, logs });
          }),
          catchError((error: { message?: string }) =>
            of(OverviewActions.loadDashboardFailure({ error: error.message ?? 'Failed to load dashboard' })),
          ),
        ),
      ),
    ),
  );
}

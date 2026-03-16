import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AgentService } from '../../core/services/agent.service';
import { AgentActions } from './agents.actions';

@Injectable()
export class AgentsEffects {
  private readonly actions$ = inject(Actions);
  private readonly agentService = inject(AgentService);

  readonly loadAgents$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AgentActions.loadAgents),
      switchMap(() =>
        this.agentService.getAll().pipe(
          map((agents) => AgentActions.loadAgentsSuccess({ agents })),
          catchError((error: { message?: string }) =>
            of(AgentActions.loadAgentsFailure({ error: error.message ?? 'Failed to load agents' })),
          ),
        ),
      ),
    ),
  );

  readonly loadStats$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AgentActions.loadStats),
      switchMap(() =>
        this.agentService.getStats().pipe(
          map((stats) => AgentActions.loadStatsSuccess({ stats })),
          catchError((error: { message?: string }) =>
            of(AgentActions.loadStatsFailure({ error: error.message ?? 'Failed to load stats' })),
          ),
        ),
      ),
    ),
  );
}

import { createEntityAdapter } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';
import { AgentActions } from './agents.actions';
import { AgentsState } from './agents.state';
import { AgentDetails } from '../../core/models/api.models';

export const agentsAdapter = createEntityAdapter<AgentDetails>({
  selectId: (agent) => agent.agentId,
});

export const initialAgentsState: AgentsState = agentsAdapter.getInitialState({
  loading: false,
  error: null,
  stats: null,
});

export const agentsReducer = createReducer(
  initialAgentsState,
  on(AgentActions.loadAgents, (state) => ({ ...state, loading: true, error: null })),
  on(AgentActions.loadAgentsSuccess, (state, { agents }) =>
    agentsAdapter.setAll(agents, { ...state, loading: false }),
  ),
  on(AgentActions.loadAgentsFailure, (state, { error }) => ({ ...state, loading: false, error })),
  on(AgentActions.loadStats, (state) => ({ ...state, loading: true, error: null })),
  on(AgentActions.loadStatsSuccess, (state, { stats }) => ({ ...state, loading: false, stats })),
  on(AgentActions.loadStatsFailure, (state, { error }) => ({ ...state, loading: false, error })),
);

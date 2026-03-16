import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AgentsState } from './agents.state';
import { agentsAdapter } from './agents.reducer';

export const selectAgentsState = createFeatureSelector<AgentsState>('agents');

const { selectAll } = agentsAdapter.getSelectors();

export const selectAllAgents = createSelector(selectAgentsState, selectAll);
export const selectAgentsLoading = createSelector(selectAgentsState, (state) => state.loading);
export const selectAgentStats = createSelector(selectAgentsState, (state) => state.stats);
export const selectAgentsError = createSelector(selectAgentsState, (state) => state.error);

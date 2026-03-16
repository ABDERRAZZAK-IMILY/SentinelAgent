import { ActionReducerMap } from '@ngrx/store';
import { agentsReducer } from './agents/agents.reducer';
import { AgentsState } from './agents/agents.state';
import { alertsReducer } from './alerts/alerts.reducer';
import { AlertsState } from './alerts/alerts.state';
import { overviewReducer } from '../features/overview/store/overview.reducer';
import { OverviewState } from '../features/overview/store/overview.state';

export interface AppState {
  agents: AgentsState;
  alerts: AlertsState;
  overview: OverviewState;
}

export const appReducers: ActionReducerMap<AppState> = {
  agents: agentsReducer,
  alerts: alertsReducer,
  overview: overviewReducer,
};

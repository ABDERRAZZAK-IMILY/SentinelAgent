import { createReducer, on } from '@ngrx/store';
import { OverviewActions } from './overview.actions';
import { initialOverviewState } from './overview.state';

export const overviewReducer = createReducer(
  initialOverviewState,
  on(OverviewActions.loadDashboard, (state) => ({ ...state, loading: true, error: null })),
  on(OverviewActions.loadDashboardSuccess, (state, { unitSummary, performance, logs }) => ({
    ...state,
    unitSummary,
    performance,
    logs,
    loading: false,
  })),
  on(OverviewActions.loadDashboardFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),
);

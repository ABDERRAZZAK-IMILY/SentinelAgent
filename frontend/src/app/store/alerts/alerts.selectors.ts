import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AlertsState } from './alerts.state';
import { alertsAdapter } from './alerts.reducer';

export const selectAlertsState = createFeatureSelector<AlertsState>('alerts');

const { selectAll } = alertsAdapter.getSelectors();

export const selectAllAlerts = createSelector(selectAlertsState, selectAll);
export const selectAlertsLoading = createSelector(selectAlertsState, (state) => state.loading);
export const selectAlertStats = createSelector(selectAlertsState, (state) => state.stats);
export const selectAlertsError = createSelector(selectAlertsState, (state) => state.error);
export const selectRecentAlerts = createSelector(selectAllAlerts, (alerts) =>
  [...alerts]
    .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
    .slice(0, 6),
);
export const selectCriticalAlertsCount = createSelector(
  selectAllAlerts,
  (alerts) => alerts.filter((alert) => alert.severity === 'CRITICAL' && alert.status !== 'RESOLVED').length,
);

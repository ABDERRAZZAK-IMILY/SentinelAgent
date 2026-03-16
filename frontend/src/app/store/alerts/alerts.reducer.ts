import { createEntityAdapter } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';
import { AlertActions } from './alerts.actions';
import { AlertsState } from './alerts.state';
import { Alert } from '../../core/models/api.models';

export const alertsAdapter = createEntityAdapter<Alert>({
  selectId: (alert) => alert.id,
});

export const initialAlertsState: AlertsState = alertsAdapter.getInitialState({
  stats: null,
  loading: false,
  error: null,
});

export const alertsReducer = createReducer(
  initialAlertsState,
  on(AlertActions.loadAlerts, (state) => ({ ...state, loading: true, error: null })),
  on(AlertActions.loadAlertsSuccess, (state, { alerts }) =>
    alertsAdapter.setAll(alerts, { ...state, loading: false }),
  ),
  on(AlertActions.loadAlertsFailure, (state, { error }) => ({ ...state, loading: false, error })),
  on(AlertActions.loadAlertStats, (state) => ({ ...state, loading: true, error: null })),
  on(AlertActions.loadAlertStatsSuccess, (state, { stats }) => ({ ...state, loading: false, stats })),
  on(AlertActions.loadAlertStatsFailure, (state, { error }) => ({ ...state, loading: false, error })),
  on(AlertActions.updateAlertStatusSuccess, (state, { alert }) =>
    alertsAdapter.updateOne({ id: alert.id, changes: alert }, state),
  ),
  on(AlertActions.updateAlertStatusFailure, (state, { error }) => ({ ...state, error })),
);

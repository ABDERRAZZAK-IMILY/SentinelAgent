import { createFeatureSelector, createSelector } from '@ngrx/store';
import { OverviewState } from './overview.state';

export const selectOverviewState = createFeatureSelector<OverviewState>('overview');

export const selectOverviewLoading = createSelector(selectOverviewState, (state) => state.loading);
export const selectOverviewUnitSummary = createSelector(selectOverviewState, (state) => state.unitSummary);
export const selectOverviewPerformance = createSelector(selectOverviewState, (state) => state.performance);
export const selectOverviewLogs = createSelector(selectOverviewState, (state) => state.logs);

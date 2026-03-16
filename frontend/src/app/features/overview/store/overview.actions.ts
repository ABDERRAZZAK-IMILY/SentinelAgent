import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { ActivityLog, MetricSummary } from '../../../core/models/view.model';

export const OverviewActions = createActionGroup({
  source: 'Overview API',
  events: {
    'Load Dashboard': emptyProps(),
    'Load Dashboard Success': props<{
      unitSummary: MetricSummary[];
      performance: MetricSummary[];
      logs: ActivityLog[];
    }>(),
    'Load Dashboard Failure': props<{ error: string }>(),
  },
});

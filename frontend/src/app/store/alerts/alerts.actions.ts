import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { Alert, AlertStats } from '../../core/models/api.models';

export const AlertActions = createActionGroup({
  source: 'Alerts API',
  events: {
    'Load Alerts': props<{ status?: string; severity?: string }>(),
    'Load Alerts Success': props<{ alerts: Alert[] }>(),
    'Load Alerts Failure': props<{ error: string }>(),
    'Load Alert Stats': emptyProps(),
    'Load Alert Stats Success': props<{ stats: AlertStats }>(),
    'Load Alert Stats Failure': props<{ error: string }>(),
    'Update Alert Status': props<{ id: string; status: string }>(),
    'Update Alert Status Success': props<{ alert: Alert }>(),
    'Update Alert Status Failure': props<{ error: string }>(),
  },
});

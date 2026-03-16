import { EntityState } from '@ngrx/entity';
import { Alert, AlertStats } from '../../core/models/api.models';

export interface AlertsState extends EntityState<Alert> {
  stats: AlertStats | null;
  loading: boolean;
  error: string | null;
}

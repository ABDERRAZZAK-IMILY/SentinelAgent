import { ActivityLog, MetricSummary } from '../../../core/models/view.model';

export interface OverviewState {
  unitSummary: MetricSummary[];
  performance: MetricSummary[];
  logs: ActivityLog[];
  loading: boolean;
  error: string | null;
}

export const initialOverviewState: OverviewState = {
  unitSummary: [],
  performance: [],
  logs: [],
  loading: true,
  error: null,
};

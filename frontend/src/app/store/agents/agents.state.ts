import { EntityState } from '@ngrx/entity';
import { AgentDetails, AgentStats } from '../../core/models/api.models';

export interface AgentsState extends EntityState<AgentDetails> {
  loading: boolean;
  error: string | null;
  stats: AgentStats | null;
}

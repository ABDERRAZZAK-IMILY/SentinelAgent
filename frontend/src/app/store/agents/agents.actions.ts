import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { AgentDetails, AgentStats } from '../../core/models/api.models';

export const AgentActions = createActionGroup({
  source: 'Agents API',
  events: {
    'Load Agents': emptyProps(),
    'Load Agents Success': props<{ agents: AgentDetails[] }>(),
    'Load Agents Failure': props<{ error: string }>(),
    'Load Stats': emptyProps(),
    'Load Stats Success': props<{ stats: AgentStats }>(),
    'Load Stats Failure': props<{ error: string }>(),
  },
});

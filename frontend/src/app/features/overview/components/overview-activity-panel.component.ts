import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { ActivityLog } from '../../../core/models/view.model';

@Component({
  selector: 'app-overview-activity-panel',
  standalone: true,
  templateUrl: './overview-activity-panel.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverviewActivityPanelComponent {
  readonly loading = input.required<boolean>();
  readonly logs = input.required<ActivityLog[]>();
}

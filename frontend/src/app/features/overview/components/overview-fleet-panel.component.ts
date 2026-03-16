import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { MetricSummary } from '../../../core/models/view.model';
import { MetricCardComponent } from '../../../shared/ui/metric-card.component';

@Component({
  selector: 'app-overview-fleet-panel',
  standalone: true,
  imports: [MetricCardComponent],
  templateUrl: './overview-fleet-panel.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverviewFleetPanelComponent {
  readonly loading = input.required<boolean>();
  readonly unitSummary = input.required<MetricSummary[]>();
  readonly performance = input.required<MetricSummary[]>();
}

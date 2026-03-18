import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { QuickLink } from '../../../core/models/view.model';

@Component({
  selector: 'app-overview-control-panel',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './overview-control-panel.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverviewControlPanelComponent {
  readonly quickLinks = input.required<readonly QuickLink[]>();
}

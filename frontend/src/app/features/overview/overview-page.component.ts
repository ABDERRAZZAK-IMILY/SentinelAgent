import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { AppFooterComponent } from '../../shared/components/app-footer.component';
import { QuickLink } from '../../core/models/view.model';
import { Store } from '@ngrx/store';
import { combineLatest } from 'rxjs';
import { OverviewActions } from './store/overview.actions';
import {
  selectOverviewLoading,
  selectOverviewLogs,
  selectOverviewPerformance,
  selectOverviewUnitSummary,
} from './store/overview.selectors';
import { OverviewControlPanelComponent } from './components/overview-control-panel.component';
import { OverviewFleetPanelComponent } from './components/overview-fleet-panel.component';
import { OverviewActivityPanelComponent } from './components/overview-activity-panel.component';

@Component({
  selector: 'app-overview-page',
  standalone: true,
  imports: [
    AppFooterComponent,
    AsyncPipe,
    OverviewControlPanelComponent,
    OverviewFleetPanelComponent,
    OverviewActivityPanelComponent,
  ],
  templateUrl: './overview-page.component.html',
  styleUrl: './overview-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverviewPageComponent implements OnInit {
  private readonly store = inject(Store);

  protected readonly quickLinks: readonly QuickLink[] = [
    { label: 'Overview', icon: 'dashboard' },
    { label: 'Agent Fleet', icon: 'smart_toy' },
    { label: 'Command Logs', icon: 'terminal' },
    { label: 'Firewall', icon: 'security' },
  ];

  protected readonly vm$ = combineLatest({
    loading: this.store.select(selectOverviewLoading),
    unitSummary: this.store.select(selectOverviewUnitSummary),
    performance: this.store.select(selectOverviewPerformance),
    logs: this.store.select(selectOverviewLogs),
  });

  ngOnInit() {
    this.store.dispatch(OverviewActions.loadDashboard());
  }
}

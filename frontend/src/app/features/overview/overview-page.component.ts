import { Component, OnInit } from '@angular/core';
import { AppFooterComponent } from '../../shared/components/app-footer.component';
import { MetricCardComponent } from '../../shared/ui/metric-card.component';
import { ActivityLog, MetricSummary, QuickLink } from '../../core/models/view.model';
import { AgentService } from '../../core/services/agent.service';
import { AlertService } from '../../core/services/alert.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-overview-page',
  standalone: true,
  imports: [MetricCardComponent, AppFooterComponent],
  templateUrl: './overview-page.component.html',
  styleUrl: './overview-page.component.css',
})
export class OverviewPageComponent implements OnInit {
  protected readonly quickLinks: readonly QuickLink[] = [
    { label: 'Overview', icon: 'dashboard' },
    { label: 'Agent Fleet', icon: 'smart_toy' },
    { label: 'Command Logs', icon: 'terminal' },
    { label: 'Firewall', icon: 'security' },
  ];

  protected unitSummary: MetricSummary[] = [];
  protected performance: MetricSummary[] = [];
  protected logs: ActivityLog[] = [];
  protected loading = true;

  constructor(
    private readonly agentService: AgentService,
    private readonly alertService: AlertService,
  ) {}

  ngOnInit() {
    forkJoin({
      agentStats: this.agentService.getStats(),
      alerts: this.alertService.getAll(),
    }).subscribe(({ agentStats, alerts }) => {
      this.unitSummary = [
        { label: 'Active Agents', value: agentStats.active.toString(), icon: 'smart_toy', hint: null },
        { label: 'Total Offline', value: (agentStats.inactive + agentStats.error + agentStats.revoked).toString(), icon: 'power_off', hint: null },
      ];

      this.performance = [
        { label: 'Total Deployed', value: (agentStats.active + agentStats.inactive + agentStats.error + agentStats.revoked).toString(), icon: 'memory', hint: 'Fleet size' },
        { label: 'Critical Errors', value: agentStats.error.toString(), icon: 'warning', hint: agentStats.error > 0 ? 'Action needed' : 'Nominal' },
        { label: 'Revoked', value: agentStats.revoked.toString(), icon: 'block', hint: 'Keys revoked' },
        { label: 'Network', value: 'OK', icon: 'wifi', hint: 'Stable link' },
      ];

      this.logs = alerts.slice(0, 5).map(alert => ({
        message: alert.description,
        timestamp: new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        category: alert.threatType,
        highlight: alert.severity === 'CRITICAL' || alert.severity === 'HIGH'
      }));

      this.loading = false;
    });
  }
}

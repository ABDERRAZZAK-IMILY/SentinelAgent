import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgClass, DecimalPipe, DatePipe } from '@angular/common';
import { TelemetryService, MetricReportDetail } from '../../core/services/telemetry.service';
import { AgentDetails } from '../../core/models/api.models';
import { Subject, switchMap, timer, takeUntil, catchError, of } from 'rxjs';
import { Store } from '@ngrx/store';
import { AgentActions } from '../../store/agents/agents.actions';
import { selectAllAgents } from '../../store/agents/agents.selectors';

@Component({
  selector: 'app-telemetry-page',
  standalone: true,
  imports: [NgClass, DecimalPipe, DatePipe],
  templateUrl: './telemetry-page.component.html',
  styleUrl: './telemetry-page.component.css',
})
export class TelemetryPageComponent implements OnInit, OnDestroy {
  protected agents: AgentDetails[] = [];
  protected selectedAgent: AgentDetails | null = null;
  protected latestReport: MetricReportDetail | null = null;
  protected historyReports: MetricReportDetail[] = [];
  protected loading = true;
  protected telemetryLoading = false;

  private readonly destroy$ = new Subject<void>();
  private readonly agentSelected$ = new Subject<string>();

  constructor(
    private readonly store: Store,
    private readonly telemetryService: TelemetryService,
  ) {}

  ngOnInit() {
    this.agentSelected$
      .pipe(
        switchMap((agentId) =>
          timer(0, 10_000).pipe(
            switchMap(() =>
              this.telemetryService.getLatest(agentId).pipe(
                catchError(() => of(null)),
              ),
            ),
          ),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((report) => {
        if (report) {
          this.latestReport = report;
        }
        this.telemetryLoading = false;
      });

    this.store.dispatch(AgentActions.loadAgents());
    this.store.dispatch(AgentActions.loadStats());

    this.store.select(selectAllAgents).pipe(takeUntil(this.destroy$)).subscribe((agents) => {
      this.agents = agents;
      this.loading = false;

      if (agents.length === 0) {
        this.selectedAgent = null;
        return;
      }

      if (!this.selectedAgent || !agents.some((agent) => agent.agentId === this.selectedAgent?.agentId)) {
        this.selectAgent(agents[0]);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected selectAgent(agent: AgentDetails): void {
    this.selectedAgent = agent;
    this.latestReport = null;
    this.historyReports = [];
    this.telemetryLoading = true;
    this.agentSelected$.next(agent.agentId);
    this.loadHistory(agent.agentId);
  }

  protected cpuTone(value: number): string {
    if (value >= 90) return 'text-error';
    if (value >= 70) return 'text-warning';
    return 'text-primary';
  }

  protected ramTone(value: number): string {
    if (value >= 90) return 'text-error';
    if (value >= 70) return 'text-warning';
    return 'text-primary';
  }

  protected barWidth(value: number): string {
    return `${Math.min(value, 100)}%`;
  }

  protected barColor(value: number): string {
    if (value >= 90) return 'bg-error';
    if (value >= 70) return 'bg-warning';
    return 'bg-primary';
  }

  protected formatBytes(bytes: number): string {
    if (bytes < 1024) return `${bytes} B/s`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB/s`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB/s`;
  }

  private loadHistory(agentId: string): void {
    this.telemetryService.getHistory(agentId, 1).subscribe((reports) => {
      this.historyReports = reports;
    });
  }
}

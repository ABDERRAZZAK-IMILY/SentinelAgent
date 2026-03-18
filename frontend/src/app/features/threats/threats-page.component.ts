import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { NgClass, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ThreatLog } from '../../core/models/view.model';
import { AlertService } from '../../core/services/alert.service';
import { Alert, AlertStats } from '../../core/models/api.models';
import { AiService } from '../../core/services/ai.service';
import { NotificationService } from '../../core/services/notification.service';
import { catchError, forkJoin, interval, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

type ThreatFilter = 'All Logs' | 'Critical' | 'Warning' | 'Resolved';
type ThreatFeedItem = ThreatLog & { alertId: string; timestamp: string; severity: string; status: string; sourceAgentId: string | null };

@Component({
  selector: 'app-threats-page',
  standalone: true,
  imports: [FormsModule, NgClass, UpperCasePipe],
  templateUrl: './threats-page.component.html',
  styleUrl: './threats-page.component.css',
})
export class ThreatsPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly notifiedCriticalAlertIds = new Set<string>();

  protected readonly filters: readonly ThreatFilter[] = ['All Logs', 'Critical', 'Warning', 'Resolved'];
  protected readonly sectorCode = 'Sector 7G';
  protected activeFilter: ThreatFilter = 'All Logs';
  protected searchTerm = '';
  protected threatLogs: ThreatFeedItem[] = [];
  protected allAlerts: Alert[] = [];
  protected stats: AlertStats | null = null;
  protected selectedAlert: Alert | null = null;
  protected detailOpen = false;
  protected loading = true;
  protected liveStatus = 'Connecting';
  protected lastRefreshLabel = 'Awaiting feed';
  protected aiBriefing = 'AI briefing is loading from the live alert feed.';
  protected aiBusy = false;
  protected criticalBanner: string | null = null;
  protected notificationPermission: NotificationPermission | 'unsupported' = 'unsupported';
  protected readonly refreshIntervalMs = 15000;

  constructor(
    private readonly alertService: AlertService,
    private readonly aiService: AiService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit() {
    this.notificationPermission = this.notificationService.permission;
    void this.notificationService.requestPermission().then((permission) => {
      this.notificationPermission = permission;
    });

    interval(this.refreshIntervalMs)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        tap(() => {
          this.liveStatus = 'Streaming';
        }),
        switchMap(() => this.loadFeed()),
      )
      .subscribe();

    this.loadFeed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe();
  }

  protected applyFilter(filter: ThreatFilter): void {
    this.activeFilter = filter;
    this.mapAlerts(this.allAlerts);
  }

  protected onSearchTermChange(): void {
    this.mapAlerts(this.allAlerts);
  }

  protected viewDetail(log: ThreatFeedItem): void {
    const alert = this.allAlerts.find((a) => a.id === log.alertId);
    if (alert) {
      this.selectedAlert = alert;
      this.detailOpen = true;
    }
  }

  protected closeDetail(): void {
    this.detailOpen = false;
    this.selectedAlert = null;
  }

  protected updateAlertStatus(alertId: string, status: string): void {
    this.alertService.updateStatus(alertId, status).subscribe((updated) => {
      const idx = this.allAlerts.findIndex((a) => a.id === updated.id);
      if (idx >= 0) this.allAlerts[idx] = updated;
      this.applyFilter(this.activeFilter);
      this.alertService.getStats().subscribe((s) => (this.stats = s));
      if (this.selectedAlert?.id === updated.id) this.selectedAlert = updated;
    });
  }

  protected requestNotificationPermission(): void {
    void this.notificationService.requestPermission().then((permission) => {
      this.notificationPermission = permission;
    });
  }

  private loadFeed() {
    return forkJoin({
      alerts: this.alertService.getAll(),
      stats: this.alertService.getStats(),
    }).pipe(
      tap(({ alerts, stats }) => {
        this.loading = false;
        this.liveStatus = 'Live';
        this.lastRefreshLabel = new Date().toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
        });
        this.stats = stats;
        this.processAlertFeed(alerts);
      }),
      catchError(() => {
        this.loading = false;
        this.liveStatus = 'Degraded';
        this.aiBriefing = 'Feed degraded. Sentinel AI could not refresh the latest alert context.';
        return of(null);
      }),
    );
  }

  private processAlertFeed(alerts: Alert[]): void {
    const sortedAlerts = [...alerts].sort((left, right) =>
      new Date(right.timestamp).getTime() - new Date(left.timestamp).getTime(),
    );

    const firstLoad = this.allAlerts.length === 0;
    this.allAlerts = sortedAlerts;
    this.mapAlerts(sortedAlerts);
    this.syncCriticalNotifications(sortedAlerts, firstLoad);
    this.refreshAiBriefing(sortedAlerts);
  }

  private mapAlerts(alerts: Alert[]): void {
    const filteredAlerts = alerts.filter((alert) => this.matchesFilter(alert) && this.matchesSearch(alert));

    this.threatLogs = filteredAlerts.map((alert) => {
      let uiLevel: 'critical' | 'warning' | 'resolved' = 'warning';
      if (alert.severity === 'CRITICAL' || alert.severity === 'HIGH') uiLevel = 'critical';
      else if (alert.status === 'RESOLVED') uiLevel = 'resolved';

      let uiActionLabel = 'Investigate';
      if (uiLevel === 'critical') uiActionLabel = 'Neutralize';
      else if (uiLevel === 'resolved') uiActionLabel = 'Archived';

      return {
        alertId: alert.id,
        level: uiLevel,
        title: alert.threatType,
        details: alert.description,
        timestamp: alert.timestamp,
        severity: alert.severity,
        status: alert.status,
        sourceAgentId: alert.sourceAgentId || null,
        time: new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        tags: [`Source: ${alert.sourceAgentId || 'Unknown'}`, `Severity: ${alert.severity}`],
        actionLabel: uiActionLabel,
      };
    });
  }

  private matchesFilter(alert: Alert): boolean {
    if (this.activeFilter === 'Critical') {
      return alert.severity === 'CRITICAL' || alert.severity === 'HIGH';
    }
    if (this.activeFilter === 'Warning') {
      return alert.severity === 'MEDIUM' || alert.severity === 'LOW';
    }
    if (this.activeFilter === 'Resolved') {
      return alert.status === 'RESOLVED';
    }
    return true;
  }

  private matchesSearch(alert: Alert): boolean {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return true;
    }

    return [
      alert.threatType,
      alert.description,
      alert.sourceAgentId,
      alert.severity,
      alert.status,
    ]
      .filter((value): value is string => Boolean(value))
      .some((value) => value.toLowerCase().includes(term));
  }

  private syncCriticalNotifications(alerts: Alert[], firstLoad: boolean): void {
    const activeCriticals = alerts.filter((alert) => alert.severity === 'CRITICAL' && alert.status !== 'RESOLVED');

    if (firstLoad) {
      activeCriticals.forEach((alert) => this.notifiedCriticalAlertIds.add(alert.id));
    } else {
      activeCriticals
        .filter((alert) => !this.notifiedCriticalAlertIds.has(alert.id))
        .forEach((alert) => {
          this.notifiedCriticalAlertIds.add(alert.id);
          this.notificationService.notify(`Critical threat in ${this.sectorCode}`, {
            body: `${alert.threatType}: ${alert.description}`,
            tag: alert.id,
          });
        });
    }

    const latestCritical = activeCriticals[0];
    this.criticalBanner = latestCritical
      ? `${latestCritical.threatType} detected from ${latestCritical.sourceAgentId || 'unknown source'} at ${new Date(latestCritical.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}.`
      : null;
  }

  private refreshAiBriefing(alerts: Alert[]): void {
    const summaryAlerts = alerts.slice(0, 5);
    const feedSignature = summaryAlerts
      .map((alert) => `${alert.id}:${alert.status}:${alert.severity}`)
      .join('|');

    if (!feedSignature || this.aiBusy || feedSignature === this.lastAiSignature) {
      return;
    }

    this.lastAiSignature = feedSignature;
    this.aiBusy = true;

    const prompt = [
      `Summarize the live SentinelAgent threat feed for ${this.sectorCode}.`,
      'Return a concise operations briefing with risk level, the main threat driver, and the next action.',
      'Current alerts:',
      ...summaryAlerts.map(
        (alert, index) =>
          `${index + 1}. ${alert.severity} ${alert.threatType} from ${alert.sourceAgentId || 'unknown'} with status ${alert.status}. ${alert.description}`,
      ),
    ].join('\n');

    this.aiService
      .chat(prompt, summaryAlerts[0]?.sourceAgentId ?? null)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        catchError(() => of({ response: 'AI briefing unavailable. Continue triage using the live feed and alert recommendations.' })),
      )
      .subscribe((response) => {
        this.aiBriefing = response.response;
        this.aiBusy = false;
      });
  }

  private lastAiSignature = '';
}

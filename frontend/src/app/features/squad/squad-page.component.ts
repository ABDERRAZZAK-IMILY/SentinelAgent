import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentService } from '../../core/services/agent.service';
import { AgentCommand, Alert, AgentDetails } from '../../core/models/api.models';
import { TelemetryService } from '../../core/services/telemetry.service';
import { AlertService } from '../../core/services/alert.service';
import { AgentCard } from '../../core/models/view.model';
import { Store } from '@ngrx/store';
import { AgentActions } from '../../store/agents/agents.actions';
import { selectAllAgents } from '../../store/agents/agents.selectors';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-squad-page',
  standalone: true,
  imports: [NgClass, FormsModule],
  templateUrl: './squad-page.component.html',
  styleUrl: './squad-page.component.css',
})
export class SquadPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  protected agents: (AgentCard & { agentId: string })[] = [];
  protected alerts: Alert[] = [];
  protected selectedAgentId: string | null = null;
  protected commandHistory: AgentCommand[] = [];
  protected commandInput = '';
  protected sending = false;
  protected historyOpen = false;
  protected actionMenuAlertId: string | null = null;
  protected actionType: 'TERMINATE_PROCESS' | 'BLOCK_IP' = 'TERMINATE_PROCESS';
  protected actionPid = '';
  protected actionIp = '';
  protected actionSubmitting = false;

  constructor(
    private readonly store: Store,
    private readonly agentService: AgentService,
    private readonly telemetryService: TelemetryService,
    private readonly alertService: AlertService,
  ) {}

  ngOnInit() {
    this.store.dispatch(AgentActions.loadAgents());
    this.store.dispatch(AgentActions.loadStats());

    this.store.select(selectAllAgents).pipe(takeUntilDestroyed(this.destroyRef)).subscribe((backendAgents) => {
      this.agents = backendAgents.map((agent) => this.toAgentCard(agent));

      // Load latest telemetry for each agent
      for (const squadAgent of this.agents) {
        this.telemetryService.getLatest(squadAgent.agentId).subscribe({
          next: (report) => {
            this.agents = this.agents.map((agent) =>
              agent.agentId === squadAgent.agentId
                ? {
                    ...agent,
                    cpu: Math.round(report.cpuUsage),
                    ram: Math.round(report.ramUsedPercent),
                  }
                : agent,
            );
          },
          error: () => {},
        });
      }
    });

    this.loadAlerts();
  }

  protected openCommandPanel(agentId: string): void {
    this.selectedAgentId = agentId;
    this.historyOpen = true;
    this.commandInput = '';
    this.loadCommandHistory(agentId);
  }

  protected closeCommandPanel(): void {
    this.historyOpen = false;
    this.selectedAgentId = null;
    this.commandHistory = [];
  }

  protected sendCommand(): void {
    if (!this.selectedAgentId || !this.commandInput.trim()) return;
    this.sending = true;
    this.agentService.sendCommand(this.selectedAgentId, this.commandInput.trim()).subscribe({
      next: () => {
        this.sending = false;
        this.commandInput = '';
        this.loadCommandHistory(this.selectedAgentId!);
      },
      error: () => (this.sending = false),
    });
  }

  protected openActionMenu(alertId: string): void {
    this.actionMenuAlertId = alertId;
    this.actionType = 'TERMINATE_PROCESS';
    this.actionPid = '';
    this.actionIp = '';
  }

  protected closeActionMenu(): void {
    this.actionMenuAlertId = null;
    this.actionPid = '';
    this.actionIp = '';
    this.actionSubmitting = false;
  }

  protected confirmAlertAction(alert: Alert): void {
    const targetAgentId = alert.sourceAgentId;
    if (!targetAgentId) {
      return;
    }

    const payload = this.actionType === 'TERMINATE_PROCESS'
      ? { pid: Number(this.actionPid) }
      : { ip: this.actionIp.trim() };

    if (this.actionType === 'TERMINATE_PROCESS' && (!this.actionPid || Number.isNaN(payload.pid))) {
      return;
    }
    if (this.actionType === 'BLOCK_IP' && !this.actionIp.trim()) {
      return;
    }

    this.actionSubmitting = true;
    this.agentService.sendCommand(targetAgentId, this.actionType, payload).subscribe({
      next: () => {
        this.actionSubmitting = false;
        this.closeActionMenu();
      },
      error: () => {
        this.actionSubmitting = false;
      },
    });
  }

  protected isActionMenuOpen(alertId: string): boolean {
    return this.actionMenuAlertId === alertId;
  }

  private loadAlerts(): void {
    this.alertService.getAll().subscribe((alerts) => {
      this.alerts = alerts
        .slice()
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
        .slice(0, 8);
    });
  }

  private loadCommandHistory(agentId: string): void {
    this.agentService.getCommandHistory(agentId).subscribe((history) => {
      this.commandHistory = history;
    });
  }

  private toAgentCard(agent: AgentDetails): AgentCard & { agentId: string } {
    let uiStatus: 'Online' | 'Standby' | 'Alert' = 'Standby';
    if (agent.status === 'ERROR') uiStatus = 'Alert';
    else if (agent.status === 'ACTIVE') uiStatus = 'Online';

    return {
      agentId: agent.agentId,
      name: agent.hostname,
      code: agent.agentId,
      status: uiStatus,
      mission: agent.operatingSystem + ' / ' + agent.ipAddress,
      cpu: 0,
      ram: 0,
    };
  }
}

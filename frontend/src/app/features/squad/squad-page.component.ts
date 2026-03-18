import { Component, OnInit } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentService } from '../../core/services/agent.service';
import { AgentCommand, Alert } from '../../core/models/api.models';
import { TelemetryService } from '../../core/services/telemetry.service';
import { AlertService } from '../../core/services/alert.service';
import { AgentCard } from '../../core/models/view.model';

@Component({
  selector: 'app-squad-page',
  standalone: true,
  imports: [NgClass, FormsModule],
  templateUrl: './squad-page.component.html',
  styleUrl: './squad-page.component.css',
})
export class SquadPageComponent implements OnInit {
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
    private readonly agentService: AgentService,
    private readonly telemetryService: TelemetryService,
    private readonly alertService: AlertService,
  ) {}

  ngOnInit() {
    this.agentService.getAll().subscribe((backendAgents) => {
      this.agents = backendAgents.map((a) => {
        let uiStatus: 'Online' | 'Standby' | 'Alert' = 'Standby';
        if (a.status === 'ERROR') uiStatus = 'Alert';
        else if (a.status === 'ACTIVE') uiStatus = 'Online';

        return {
          agentId: a.agentId,
          name: a.hostname,
          code: a.agentId,
          status: uiStatus,
          mission: a.operatingSystem + ' / ' + a.ipAddress,
          cpu: 0,
          ram: 0,
        };
      });

      // Load latest telemetry for each agent
      for (const agent of this.agents) {
        this.telemetryService.getLatest(agent.agentId).subscribe({
          next: (report) => {
            const a = this.agents.find((x) => x.agentId === agent.agentId);
            if (a) {
              (a as any).cpu = Math.round(report.cpuUsage);
              (a as any).ram = Math.round(report.ramUsedPercent);
            }
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
}

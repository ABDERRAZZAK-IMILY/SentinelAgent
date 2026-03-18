import { Component, OnInit } from '@angular/core';
import { NgClass, DecimalPipe } from '@angular/common';
import { SentinelUnit } from '../../core/models/view.model';
import { AgentService } from '../../core/services/agent.service';
import { AiService } from '../../core/services/ai.service';
import { AlertService } from '../../core/services/alert.service';
import { AiStatus, AlertStats } from '../../core/models/api.models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-system-page',
  standalone: true,
  imports: [NgClass, DecimalPipe],
  templateUrl: './system-page.component.html',
  styleUrl: './system-page.component.css',
})
export class SystemPageComponent implements OnInit {
  protected sentinels: SentinelUnit[] = [];
  protected aiStatus: AiStatus | null = null;
  protected alertStats: AlertStats | null = null;
  protected training = false;
  protected trainMessage = '';

  constructor(
    private readonly agentService: AgentService,
    private readonly aiService: AiService,
    private readonly alertService: AlertService,
  ) {}

  ngOnInit() {
    forkJoin({
      agents: this.agentService.getAll(),
      aiStatus: this.aiService.getStatus(),
      alertStats: this.alertService.getStats(),
    }).subscribe(({ agents, aiStatus, alertStats }) => {
      this.sentinels = agents.map((a) => {
        const isError = a.status === 'ERROR' || a.status === 'REVOKED';
        return {
          name: a.hostname,
          summary: `Version: ${a.agentVersion} | OS: ${a.operatingSystem}`,
          status: isError ? 'Watch' : 'Nominal',
          zone: 'Global Network',
        };
      });
      this.aiStatus = aiStatus;
      this.alertStats = alertStats;
    });
  }

  protected triggerTraining(): void {
    this.training = true;
    this.trainMessage = '';
    this.aiService.train().subscribe({
      next: (result) => {
        this.training = false;
        this.trainMessage = result.message;
        // Refresh AI status
        this.aiService.getStatus().subscribe((s) => (this.aiStatus = s));
      },
      error: () => {
        this.training = false;
        this.trainMessage = 'Training request failed.';
      },
    });
  }
}

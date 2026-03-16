import { Component, OnInit } from '@angular/core';
import { NgClass, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../core/services/report.service';
import { AlertService } from '../../core/services/alert.service';
import { AgentService } from '../../core/services/agent.service';
import { Report, GenerateReportRequest, AgentDetails } from '../../core/models/api.models';
import { forkJoin } from 'rxjs';

interface ReportRow extends Report {
  uiSeverity: 'success' | 'warning' | 'error';
}

@Component({
  selector: 'app-reports-page',
  standalone: true,
  imports: [NgClass, DatePipe, FormsModule],
  templateUrl: './reports-page.component.html',
  styleUrl: './reports-page.component.css',
})
export class ReportsPageComponent implements OnInit {
  protected reports: ReportRow[] = [];
  protected agents: AgentDetails[] = [];
  protected terminal: string[] = [];
  protected generating = false;
  protected loadingReports = true;
  protected generateError: string | null = null;

  // Form fields
  protected selectedAgentId = '';
  protected reportType = 'AI_SECURITY_SUMMARY';
  protected fromDate = '';
  protected toDate = '';

  protected readonly reportTypes = [
    'AI_SECURITY_SUMMARY',
    'THREAT_ANALYSIS',
    'TELEMETRY_REPORT',
    'INCIDENT_REPORT',
  ];

  constructor(
    private readonly reportService: ReportService,
    private readonly alertService: AlertService,
    private readonly agentService: AgentService,
  ) {}

  ngOnInit() {
    forkJoin({
      agents: this.agentService.getAll(),
      alerts: this.alertService.getAll(),
    }).subscribe(({ agents, alerts }) => {
      this.agents = agents;
      if (agents.length > 0 && !this.selectedAgentId) {
        this.selectedAgentId = agents[0].agentId;
      }
      this.terminal = alerts.slice(0, 8).map(
        a => `[${new Date(a.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}] ${a.threatType}: ${a.description}`
      );
    });
    this.loadReports();
  }

  protected generateReport(): void {
    if (!this.selectedAgentId) {
      this.generateError = 'Please select an agent.';
      return;
    }
    this.generating = true;
    this.generateError = null;

    const request: GenerateReportRequest = {
      agentId: this.selectedAgentId,
      reportType: this.reportType || undefined,
      fromDate: this.fromDate ? `${this.fromDate}T00:00:00` : undefined,
      toDate: this.toDate ? `${this.toDate}T23:59:59` : undefined,
    };

    this.reportService.generate(request).subscribe({
      next: () => {
        this.generating = false;
        this.loadReports();
      },
      error: (err) => {
        this.generating = false;
        this.generateError = err?.error || 'Report generation failed. Check AI service.';
      },
    });
  }

  protected downloadReport(reportId: string): void {
    this.reportService.download(reportId).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${reportId}.txt`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  protected severityClass(s: 'success' | 'warning' | 'error'): string {
    return s === 'error' ? 'text-red-400 border-red-500/50' :
           s === 'warning' ? 'text-yellow-400 border-yellow-500/50' :
           'text-primary border-primary/50';
  }

  private loadReports(): void {
    this.loadingReports = true;
    this.reportService.getAll().subscribe({
      next: (list) => {
        this.reports = list.map(r => ({
          ...r,
          uiSeverity: this.toSeverity(r.aiSummary),
        }));
        this.loadingReports = false;
      },
      error: () => { this.loadingReports = false; },
    });
  }

  protected countBySeverity(sev: 'success' | 'warning' | 'error'): number {
    return this.reports.filter(r => r.uiSeverity === sev).length;
  }

  private toSeverity(summary: string | null): 'success' | 'warning' | 'error' {
    if (!summary) return 'success';
    const s = summary.toLowerCase();
    if (s.includes('high') || s.includes('critical') || s.includes('breach')) return 'error';
    if (s.includes('medium') || s.includes('warning') || s.includes('suspicious')) return 'warning';
    return 'success';
  }
}

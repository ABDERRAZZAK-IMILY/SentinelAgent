export interface ActivityLog {
  readonly message: string;
  readonly timestamp: string;
  readonly category: string;
  readonly highlight?: boolean;
}

export interface ChatMessage {
  readonly author: 'ai' | 'agent';
  readonly body: string;
  readonly timestamp: string;
}

export interface ReportCard {
  readonly title: string;
  readonly summary: string;
  readonly confidence: number;
  readonly severity: 'success' | 'warning' | 'error';
}

export interface NetworkNode {
  readonly name: string;
  readonly status: string;
  readonly tone: 'success' | 'warning' | 'error' | 'neutral';
}

export interface AgentCard {
  readonly name: string;
  readonly code: string;
  readonly status: 'Online' | 'Standby' | 'Alert';
  readonly mission: string;
  readonly cpu: number;
  readonly ram: number;
}

export interface ThreatLog {
  readonly level: 'critical' | 'warning' | 'resolved';
  readonly title: string;
  readonly details: string;
  readonly time: string;
  readonly tags: readonly string[];
  readonly actionLabel: string;
}

export interface ToggleSetting {
  readonly label: string;
  readonly description: string;
  readonly enabled: boolean;
}

export interface QuickLink {
  readonly label: string;
  readonly icon: string;
  readonly route: string;
}

export interface MetricSummary {
  readonly label: string;
  readonly value: string;
  readonly icon: string;
  readonly hint: string | null;
}

export interface SentinelUnit {
  readonly name: string;
  readonly summary: string;
  readonly status: 'Nominal' | 'Watch';
  readonly zone: string;
}

export interface SectorLoad {
  readonly name: string;
  readonly load: number;
}

export interface ThemeOption {
  readonly name: string;
  readonly active: boolean;
}

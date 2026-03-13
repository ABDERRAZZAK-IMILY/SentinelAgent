
// ── Auth ──────────────────────────────────────────────────────────────
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

// ── Agent ─────────────────────────────────────────────────────────────
export interface AgentDetails {
  agentId: string;
  hostname: string;
  operatingSystem: string;
  agentVersion: string;
  ipAddress: string;
  status: string;
  registeredAt: string;
  lastHeartbeat: string;
}

export interface AgentStats {
  active: number;
  inactive: number;
  revoked: number;
  error: number;
}

export interface AgentCommand {
  id: string;
  agentId: string;
  command: string;
  parameters: string;
  status: string;
  resultMessage: string | null;
  issuedAt: string;
  executedAt: string | null;
}

// ── Alert ─────────────────────────────────────────────────────────────
export interface Alert {
  id: string;
  severity: string;
  threatType: string;
  description: string;
  recommendation: string;
  sourceAgentId: string;
  status: string;
  timestamp: string;
}

export interface AlertStats {
  CRITICAL: number;
  HIGH: number;
  MEDIUM: number;
  LOW: number;
  NEW: number;
  REVIEWED: number;
  RESOLVED: number;
}

// ── Report ────────────────────────────────────────────────────────────
export interface Report {
  id: string;
  agentId: string | null;
  reportType: string | null;
  contentUrl: string | null;
  aiSummary: string | null;
  generatedAt: string;
  fromDate: string | null;
  toDate: string | null;
}

// ── Telemetry ─────────────────────────────────────────────────────────
export interface MetricReport {
  id: string;
  agentId: string;
  hostname: string;
  cpuUsage: number;
  ramUsedPercent: number;
  ramTotalMb: number;
  diskUsedPercent: number;
  diskTotalGb: number;
  bytesSentSec: number;
  bytesRecvSec: number;
  receivedAt: string;
}

// ── AI ────────────────────────────────────────────────────────────────
export interface AiStatus {
  status: string;
  modelVersion: string;
  lastTrainingDate: string;
  accuracy: number;
  ragDatabaseStatus: string;
}

export interface ChatRequest {
  message: string;
  agentId?: string | null;
}

export interface ChatResponse {
  response: string;
}

// ── User ──────────────────────────────────────────────────────────────
export interface UserInfo {
  id: string;
  username: string;
  roles: string[];
}

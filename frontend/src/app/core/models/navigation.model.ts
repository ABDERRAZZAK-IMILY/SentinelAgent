export interface NavItem {
  readonly label: string;
  readonly route: string;
  readonly icon: string;
}

export interface StatusMetric {
  readonly label: string;
  readonly value: string;
  readonly tone?: 'success' | 'warning' | 'error' | 'neutral';
}

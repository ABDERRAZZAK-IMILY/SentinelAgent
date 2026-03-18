import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.css',
})
export class StatusBadgeComponent {
  readonly text = input.required<string>();
  readonly tone = input<'success' | 'warning' | 'error' | 'neutral'>('neutral');

  protected readonly toneClass = computed(() => {
    switch (this.tone()) {
      case 'success':
        return 'badge-success';
      case 'warning':
        return 'badge-warning';
      case 'error':
        return 'badge-error';
      default:
        return 'badge-ghost';
    }
  });
}

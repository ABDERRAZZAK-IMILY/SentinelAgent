import { Component, OnInit } from '@angular/core';
import { DecimalPipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AiStatus, AgentStats, AlertStats, UserInfo } from '../../core/models/api.models';
import { AgentService } from '../../core/services/agent.service';
import { AiService } from '../../core/services/ai.service';
import { AlertService } from '../../core/services/alert.service';
import { NotificationService } from '../../core/services/notification.service';
import { UserService } from '../../core/services/user.service';

interface SecurityToggle {
  key: string;
  label: string;
  description: string;
  enabled: boolean;
}

interface UiPreferences {
  alias: string;
  theme: string;
  sound: string;
  autoRefreshSeconds: number;
  desktopNotifications: boolean;
}

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [NgClass, DecimalPipe, FormsModule],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.css',
})
export class SettingsPageComponent implements OnInit {
  protected readonly themeOptions = ['Radar Grid', 'Oceanic Ops', 'Solarized Command'];
  protected readonly soundOptions = ['chime', 'pulse', 'silent'];
  protected readonly refreshOptions = [15, 30, 60, 300];
  protected readonly roleOptions = ['ANALYST', 'ADMIN'];

  protected preferences: UiPreferences = this.getDefaultPreferences();
  protected securitySettings: SecurityToggle[] = this.getDefaultSecuritySettings();

  protected users: UserInfo[] = [];
  protected showCreateForm = false;
  protected newUsername = '';
  protected newPassword = '';
  protected newRole = 'ANALYST';

  protected aiStatus: AiStatus | null = null;
  protected agentStats: AgentStats | null = null;
  protected alertStats: AlertStats | null = null;
  protected snapshotLoadedAt = '';

  protected loadingSnapshot = false;
  protected creatingUser = false;
  protected settingsNotice = '';
  protected settingsError = '';

  private readonly preferencesKey = 'sentinel_ui_preferences';
  private readonly securityKey = 'sentinel_security_settings';

  constructor(
    private readonly userService: UserService,
    private readonly agentService: AgentService,
    private readonly alertService: AlertService,
    private readonly aiService: AiService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.loadLocalState();
    this.loadUsers();
    this.refreshSnapshot();
  }

  protected refreshSnapshot(): void {
    this.clearMessages();
    this.loadingSnapshot = true;

    forkJoin({
      users: this.userService.getAll(),
      aiStatus: this.aiService.getStatus(),
      agentStats: this.agentService.getStats(),
      alertStats: this.alertService.getStats(),
    }).subscribe({
      next: ({ users, aiStatus, agentStats, alertStats }) => {
        this.users = users;
        this.aiStatus = aiStatus;
        this.agentStats = agentStats;
        this.alertStats = alertStats;
        this.snapshotLoadedAt = new Date().toISOString();
        this.loadingSnapshot = false;

        if (!this.preferences.alias && users[0]) {
          this.preferences.alias = users[0].username;
          this.persistPreferences();
        }

        this.settingsNotice = 'Live settings snapshot refreshed.';
      },
      error: () => {
        this.loadingSnapshot = false;
        this.settingsError = 'Unable to load live data from backend.';
      },
    });
  }

  protected toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.newUsername = '';
    this.newPassword = '';
    this.newRole = 'ANALYST';
  }

  protected createUser(): void {
    if (!this.newUsername || !this.newPassword || this.creatingUser) {
      return;
    }

    this.clearMessages();
    this.creatingUser = true;

    this.userService.create({
      username: this.newUsername,
      password: this.newPassword,
      role: this.newRole,
    }).subscribe({
      next: () => {
        this.creatingUser = false;
        this.showCreateForm = false;
        this.settingsNotice = 'User created successfully.';
        this.loadUsers();
      },
      error: () => {
        this.creatingUser = false;
        this.settingsError = 'Failed to create user.';
      },
    });
  }

  protected deleteUser(user: UserInfo): void {
    this.clearMessages();
    this.userService.delete(user.id).subscribe({
      next: () => {
        this.settingsNotice = 'User removed.';
        this.loadUsers();
      },
      error: () => {
        this.settingsError = 'Failed to remove user.';
      },
    });
  }

  protected toggleSecurity(setting: SecurityToggle): void {
    this.securitySettings = this.securitySettings.map((item) =>
      item.key === setting.key ? { ...item, enabled: !item.enabled } : item,
    );
    this.persistSecuritySettings();
    this.settingsNotice = 'Security setting updated locally.';
  }

  protected savePreferences(): void {
    this.clearMessages();
    this.preferences.alias = this.preferences.alias.trim();
    if (!this.preferences.alias && this.users[0]) {
      this.preferences.alias = this.users[0].username;
    }

    this.persistPreferences();
    this.settingsNotice = 'Preferences saved.';
  }

  protected resetDefaults(): void {
    this.preferences = this.getDefaultPreferences();
    this.securitySettings = this.getDefaultSecuritySettings();
    this.persistPreferences();
    this.persistSecuritySettings();
    this.settingsNotice = 'Defaults restored.';
  }

  protected async toggleDesktopNotifications(): Promise<void> {
    this.clearMessages();

    if (!this.notificationService.supported) {
      this.settingsError = 'Desktop notifications are not supported by this browser.';
      return;
    }

    if (this.preferences.desktopNotifications) {
      this.preferences.desktopNotifications = false;
      this.persistPreferences();
      this.settingsNotice = 'Desktop notifications disabled in UI.';
      return;
    }

    const permission = await this.notificationService.requestPermission();
    if (permission !== 'granted') {
      this.settingsError = 'Permission not granted.';
      return;
    }

    this.preferences.desktopNotifications = true;
    this.persistPreferences();
    this.settingsNotice = 'Desktop notifications enabled.';
  }

  protected dismissMessage(): void {
    this.clearMessages();
  }

  protected totalAlerts(): number {
    if (!this.alertStats) {
      return 0;
    }

    return this.alertStats.CRITICAL + this.alertStats.HIGH + this.alertStats.MEDIUM + this.alertStats.LOW;
  }

  protected snapshotTimestamp(): string {
    if (!this.snapshotLoadedAt) {
      return '-';
    }

    return new Date(this.snapshotLoadedAt).toLocaleString();
  }

  protected notificationPermissionLabel(): string {
    if (!this.notificationService.supported) {
      return 'Unsupported';
    }

    return this.notificationService.permission;
  }

  protected roleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'bg-primary text-black' : 'bg-info text-black';
  }

  protected isCreateDisabled(): boolean {
    return this.creatingUser || !this.newUsername || !this.newPassword;
  }

  protected trackByUser(_: number, user: UserInfo): string {
    return user.id;
  }

  protected trackBySetting(_: number, setting: SecurityToggle): string {
    return setting.key;
  }

  protected trackByText(_: number, value: string | number): string | number {
    return value;
  }

  private loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (users) => {
        this.users = users;
        if (!this.preferences.alias && users[0]) {
          this.preferences.alias = users[0].username;
          this.persistPreferences();
        }
      },
      error: () => {
        this.settingsError = 'Unable to load users list.';
      },
    });
  }

  private loadLocalState(): void {
    this.preferences = this.readPreferences();
    this.securitySettings = this.readSecuritySettings();

    if (!this.notificationService.supported && this.preferences.desktopNotifications) {
      this.preferences.desktopNotifications = false;
      this.persistPreferences();
    }
  }

  private readPreferences(): UiPreferences {
    const raw = localStorage.getItem(this.preferencesKey);
    if (!raw) {
      return this.getDefaultPreferences();
    }

    try {
      const parsed = JSON.parse(raw) as Partial<UiPreferences>;
      return {
        alias: parsed.alias ?? '',
        theme: this.themeOptions.includes(parsed.theme ?? '') ? parsed.theme as string : this.getDefaultPreferences().theme,
        sound: this.soundOptions.includes(parsed.sound ?? '') ? parsed.sound as string : this.getDefaultPreferences().sound,
        autoRefreshSeconds: this.refreshOptions.includes(parsed.autoRefreshSeconds ?? 0)
          ? parsed.autoRefreshSeconds as number
          : this.getDefaultPreferences().autoRefreshSeconds,
        desktopNotifications: Boolean(parsed.desktopNotifications),
      };
    } catch {
      return this.getDefaultPreferences();
    }
  }

  private readSecuritySettings(): SecurityToggle[] {
    const raw = localStorage.getItem(this.securityKey);
    const defaults = this.getDefaultSecuritySettings();

    if (!raw) {
      return defaults;
    }

    try {
      const parsed = JSON.parse(raw) as Array<Partial<SecurityToggle>>;
      return defaults.map((item) => {
        const stored = parsed.find((x) => x.key === item.key);
        return stored ? { ...item, enabled: Boolean(stored.enabled) } : item;
      });
    } catch {
      return defaults;
    }
  }

  private persistPreferences(): void {
    localStorage.setItem(this.preferencesKey, JSON.stringify(this.preferences));
  }

  private persistSecuritySettings(): void {
    localStorage.setItem(this.securityKey, JSON.stringify(this.securitySettings));
  }

  private getDefaultPreferences(): UiPreferences {
    return {
      alias: '',
      theme: 'Radar Grid',
      sound: 'chime',
      autoRefreshSeconds: 30,
      desktopNotifications: false,
    };
  }

  private getDefaultSecuritySettings(): SecurityToggle[] {
    return [
      {
        key: 'strict-login',
        label: 'Strict Login Policy',
        description: 'Require strong password rules for newly created users.',
        enabled: true,
      },
      {
        key: 'command-approval',
        label: 'Manual Command Approval',
        description: 'Require manual review before dispatching critical agent commands.',
        enabled: false,
      },
      {
        key: 'mute-offhours',
        label: 'Mute Non-Critical Off-hours Alerts',
        description: 'Silence low-priority desktop notifications outside office hours.',
        enabled: false,
      },
    ];
  }

  private clearMessages(): void {
    this.settingsNotice = '';
    this.settingsError = '';
  }
}

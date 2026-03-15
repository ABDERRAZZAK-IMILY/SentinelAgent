import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe } from '@angular/common';
import { ChatMessage } from '../../core/models/view.model';
import { AiService } from '../../core/services/ai.service';
import { AgentService } from '../../core/services/agent.service';
import { AgentDetails } from '../../core/models/api.models';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [FormsModule, UpperCasePipe],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.css',
})
export class ChatPageComponent implements OnInit {
  private static readonly CHAT_STORAGE_KEY = 'sentinel.chat.messages';
  private static readonly CHAT_AGENT_STORAGE_KEY = 'sentinel.chat.selectedAgentId';

  protected draft = '';
  protected loading = false;
  protected agents: AgentDetails[] = [];
  protected selectedAgentId: string | null = null;

  private readonly defaultAiMessage: ChatMessage = {
    author: 'ai',
    body: 'BEEP BOOP! Mission parameters received. How can I assist your surveillance today, agent?',
    timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
  };

  protected messages: ChatMessage[] = [
    {
      author: 'ai',
      body: 'BEEP BOOP! Mission parameters received. How can I assist your surveillance today, agent?',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }
  ];

  constructor(
    private readonly aiService: AiService,
    private readonly agentService: AgentService,
  ) {}

  ngOnInit(): void {
    this.restoreChatState();

    this.agentService.getAll().subscribe({
      next: (agents) => {
        this.agents = agents;
        if (!this.selectedAgentId && agents.length > 0) {
          this.selectedAgentId = agents[0].agentId;
          this.persistSelectedAgent();
        }
      },
      error: () => {
        this.agents = [];
      },
    });
  }

  protected onAgentContextChange(_: string | null): void {
    this.persistSelectedAgent();
  }

  protected sendMessage(): void {
    if (!this.draft.trim() || this.loading) return;

    const userMessage = this.draft.trim();
    this.appendMessage({
      author: 'agent',
      body: userMessage,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    });

    this.draft = '';
    this.loading = true;

    this.aiService.chat(userMessage, this.selectedAgentId).subscribe({
      next: (response) => {
        this.appendMessage({
          author: 'ai',
          body: response.response,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        });
        this.loading = false;
      },
      error: () => {
        this.appendMessage({
          author: 'ai',
          body: 'ERROR: CONNECTION TO MAINFRAME LOST.',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        });
        this.loading = false;
      }
    });
  }

  private appendMessage(message: ChatMessage): void {
    this.messages.push(message);
    this.persistMessages();
  }

  private restoreChatState(): void {
    try {
      const storedMessages = localStorage.getItem(ChatPageComponent.CHAT_STORAGE_KEY);
      const storedAgentId = localStorage.getItem(ChatPageComponent.CHAT_AGENT_STORAGE_KEY);

      if (storedMessages) {
        const parsed = JSON.parse(storedMessages) as Array<Partial<ChatMessage>>;
        const valid = parsed.filter((message): message is ChatMessage => {
          return (
            (message.author === 'ai' || message.author === 'agent') &&
            typeof message.body === 'string' &&
            typeof message.timestamp === 'string'
          );
        });

        this.messages = valid.length > 0 ? valid : [this.defaultAiMessage];
      }

      this.selectedAgentId = storedAgentId && storedAgentId !== 'null' ? storedAgentId : null;
    } catch {
      this.messages = [this.defaultAiMessage];
      this.selectedAgentId = null;
    }
  }

  private persistMessages(): void {
    try {
      localStorage.setItem(ChatPageComponent.CHAT_STORAGE_KEY, JSON.stringify(this.messages));
    } catch {
    }
  }

  private persistSelectedAgent(): void {
    try {
      localStorage.setItem(
        ChatPageComponent.CHAT_AGENT_STORAGE_KEY,
        this.selectedAgentId ?? 'null'
      );
    } catch {
    }
  }
}

import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe } from '@angular/common';
import { ChatMessage } from '../../core/models/view.model';
import { AiService } from '../../core/services/ai.service';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [FormsModule, UpperCasePipe],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.css',
})
export class ChatPageComponent {
  protected draft = '';
  protected loading = false;

  protected messages: ChatMessage[] = [
    {
      author: 'ai',
      body: 'BEEP BOOP! Mission parameters received. How can I assist your surveillance today, agent?',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }
  ];

  constructor(private readonly aiService: AiService) {}

  protected sendMessage(): void {
    if (!this.draft.trim() || this.loading) return;

    const userMessage = this.draft.trim();
    this.messages.push({
      author: 'agent',
      body: userMessage,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    });

    this.draft = '';
    this.loading = true;

    this.aiService.chat(userMessage).subscribe({
      next: (response) => {
        this.messages.push({
          author: 'ai',
          body: response.response,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        });
        this.loading = false;
      },
      error: () => {
        this.messages.push({
          author: 'ai',
          body: 'ERROR: CONNECTION TO MAINFRAME LOST.',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        });
        this.loading = false;
      }
    });
  }
}

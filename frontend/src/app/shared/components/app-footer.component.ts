import { Component, input } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  templateUrl: './app-footer.component.html',
  styleUrl: './app-footer.component.css',
})
export class AppFooterComponent {
  readonly leftText = input.required<string>();
  readonly rightText = input.required<string>();
}

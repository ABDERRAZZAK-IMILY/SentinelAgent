import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { appRoutes } from './app/app.routes';
import { authInterceptor } from './app/core/interceptors/auth.interceptor';
import { provideStore } from '@ngrx/store';
import { appReducers } from './app/store/app.state';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { isDevMode } from '@angular/core';
import { AgentsEffects } from './app/store/agents/agents.effects';
import { AlertEffects } from './app/store/alerts/alerts.effects';
import { OverviewEffects } from './app/features/overview/store/overview.effects';

bootstrapApplication(AppComponent, {
  providers: [
    provideAnimations(),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideRouter(appRoutes, withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    provideStore(appReducers),
    provideEffects([AgentsEffects, AlertEffects, OverviewEffects]),
    provideStoreDevtools({
      maxAge: 25, 
      logOnly: !isDevMode(),
      autoPause: true,
      trace: false,
      traceLimit: 75,
    }),
    
],
}).catch((err) => console.error(err));

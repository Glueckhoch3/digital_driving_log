import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('Digital Driving Log');
  private readonly translate = inject(TranslateService);

  constructor() {
    this.translate.addLangs(['en', 'de']);
    const browserLang = this.translate.getBrowserLang();
    const supportedLanguages = ['en', 'de'];
    const languageToUse =
      browserLang && supportedLanguages.includes(browserLang) ? browserLang : 'en';
    this.translate.use(languageToUse);
  }
}

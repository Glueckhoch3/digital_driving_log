import { Component, DestroyRef, ElementRef, afterNextRender, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { watchStackedTables } from './utils/stacked-tables';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, TranslatePipe],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('Digital Driving Log');
  private readonly translate = inject(TranslateService);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    this.translate.addLangs(['en', 'de']);
    const browserLang = this.translate.getBrowserLang();
    const supportedLanguages = ['en', 'de'];
    const languageToUse =
      browserLang && supportedLanguages.includes(browserLang) ? browserLang : 'en';
    this.translate.use(languageToUse);

    // keep <html lang> in sync (screen readers pick the voice from it)
    document.documentElement.lang = languageToUse;
    this.translate.onLangChange.subscribe((event) => {
      document.documentElement.lang = event.lang;
    });

    // stacked phone layout of tables needs data-label on cells (see utils/stacked-tables.ts)
    afterNextRender(() => {
      const stop = watchStackedTables(this.host.nativeElement);
      this.destroyRef.onDestroy(stop);
    });
  }
}

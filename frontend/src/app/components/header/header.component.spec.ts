import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { Component } from '@angular/core';
import { HeaderComponent } from './header.component';

@Component({ template: '' })
class DummyComponent {}

describe('HeaderComponent', () => {
  let fixture: ComponentFixture<HeaderComponent>;
  let router: Router;
  let translate: TranslateService;

  const el = (): HTMLElement => fixture.nativeElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideTranslateService({ fallbackLang: 'en' }),
        provideRouter([
          { path: '', component: DummyComponent },
          { path: 'cars/:id', component: DummyComponent },
          { path: 'log', component: DummyComponent },
          { path: 'calculations/combined', component: DummyComponent },
          { path: 'calculations/results', component: DummyComponent },
          { path: 'manage/users', component: DummyComponent },
        ]),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    translate = TestBed.inject(TranslateService);
    translate.use('de');
    fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
  });

  async function navigate(url: string): Promise<void> {
    await router.navigateByUrl(url);
    fixture.detectChanges();
  }

  function current(selector: string): string[] {
    return Array.from(el().querySelectorAll(`${selector}[aria-current="page"]`)).map((a) =>
      (a.getAttribute('href') ?? '').toString(),
    );
  }

  it('renders the three main links in both the top bar and the tab bar', () => {
    expect(el().querySelectorAll('.top__link[href]').length).toBe(3);
    expect(el().querySelectorAll('.tabs__item[href]').length).toBe(3);
  });

  it('marks the active section, also for nested routes', async () => {
    await navigate('/cars/7');
    expect(current('.top__link')).toEqual(['/']);

    await navigate('/log');
    expect(current('.top__link')).toEqual(['/log']);

    await navigate('/calculations/results');
    expect(current('.tabs__item')).toEqual(['/calculations/combined']);
  });

  it('opens the settings menu, offers three pages and closes with Escape', async () => {
    expect(el().querySelector('.sheet')).toBeNull();

    (el().querySelector('.top__more') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(el().querySelector('.sheet')).not.toBeNull();
    expect(el().querySelectorAll('.sheet__link').length).toBe(3);
    expect(el().querySelector('.top__more')?.getAttribute('aria-expanded')).toBe('true');

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(el().querySelector('.sheet')).toBeNull();
  });

  it('closes the settings menu after navigating', async () => {
    (el().querySelector('.tabs__item[aria-haspopup]') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(el().querySelector('.sheet')).not.toBeNull();

    await navigate('/manage/users');
    expect(el().querySelector('.sheet')).toBeNull();
  });

  it('marks settings as active on manage pages', async () => {
    await navigate('/manage/users');
    expect(el().querySelector('.top__more')?.classList.contains('is-active')).toBe(true);
  });

  it('toggles the language and shows the current code', () => {
    const button = el().querySelector('.top__lang') as HTMLButtonElement;
    expect(button.textContent).toContain('DE');

    button.click();
    fixture.detectChanges();

    expect(translate.getCurrentLang()).toBe('en');
    expect(button.textContent).toContain('EN');
  });
});

import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { filter, map } from 'rxjs';
import { IconComponent, IconName } from '../icon/icon.component';

interface NavItem {
  key: 'cars' | 'log' | 'settlement';
  route: string;
  icon: IconName;
  /** URL prefixes (path without query) for which this item counts as active */
  prefixes: string[];
}

interface SettingsItem {
  labelKey: string;
  route: string;
  icon: IconName;
}

/**
 * App navigation (issue #22): top bar with horizontal links on wide screens, fixed bottom tab bar
 * on phones. Rarely used pages sit behind "Settings" (dropdown on wide screens, bottom sheet on
 * phones). The language button at the right end switches between the supported languages.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TranslateModule, IconComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  readonly settingsOpen = signal(false);
  readonly currentLang = signal(this.translate.getCurrentLang() || 'en');

  private readonly sheet = viewChild<ElementRef<HTMLElement>>('sheet');
  private lastTrigger: HTMLElement | null = null;

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );
  private readonly path = computed(() => this.url().split(/[?#]/)[0]);

  readonly navItems: NavItem[] = [
    { key: 'cars', route: '/', icon: 'car', prefixes: ['/', '/cars'] },
    { key: 'log', route: '/log', icon: 'list', prefixes: ['/log'] },
    {
      key: 'settlement',
      route: '/calculations/combined',
      icon: 'calc',
      prefixes: ['/calculations'],
    },
  ];

  readonly settingsItems: SettingsItem[] = [
    { labelKey: 'header.menu.manageUsers', route: '/manage/users', icon: 'users' },
    { labelKey: 'header.menu.manageCars', route: '/manage/cars', icon: 'car' },
    { labelKey: 'header.menu.manageCostCategories', route: '/manage/cost-categories', icon: 'tag' },
  ];

  readonly settingsActive = computed(() => this.path().startsWith('/manage'));

  constructor() {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.settingsOpen.set(false);
    });
  }

  isActive(item: NavItem): boolean {
    const path = this.path();
    return item.prefixes.some((prefix) =>
      prefix === '/' ? path === '/' : path === prefix || path.startsWith(prefix + '/'),
    );
  }

  toggleSettings(event: Event): void {
    this.lastTrigger = event.currentTarget as HTMLElement;
    const open = !this.settingsOpen();
    this.settingsOpen.set(open);
    if (open) {
      // move focus into the menu so keyboard and screen-reader users land on it
      setTimeout(() => this.sheet()?.nativeElement.querySelector<HTMLElement>('a')?.focus());
    }
  }

  closeSettings(returnFocus = false): void {
    this.settingsOpen.set(false);
    if (returnFocus) {
      this.lastTrigger?.focus();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.settingsOpen()) {
      this.closeSettings(true);
    }
  }

  /** Switches to the other supported language. */
  toggleLang(): void {
    const next = this.currentLang() === 'de' ? 'en' : 'de';
    this.translate.use(next);
    this.currentLang.set(next);
  }
}

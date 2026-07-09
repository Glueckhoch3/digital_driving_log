import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslateModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {
  isMenuOpen = signal(false);
  currentLang = signal('en');

  private readonly translate = inject(TranslateService);

  constructor() {
    this.currentLang.set(this.translate.getCurrentLang() || 'en');
  }

  toggleMenu(): void {
    this.isMenuOpen.update((value) => !value);
  }

  closeMenu(): void {
    this.isMenuOpen.set(false);
  }

  changeLang(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const lang = target.value;
    this.translate.use(lang);
    this.currentLang.set(lang);
  }

  readonly menuItems = [
    { labelKey: 'header.menu.start', route: '/' },
    { labelKey: 'header.menu.entryOverview', route: '/overview' },
    { labelKey: 'header.menu.chooseCar', route: '/cars/select' },
    { labelKey: 'header.menu.manageUsers', route: '/manage/users' },
    { labelKey: 'header.menu.manageCars', route: '/manage/cars' },
  ];
}

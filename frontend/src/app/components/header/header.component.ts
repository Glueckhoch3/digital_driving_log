import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  isMenuOpen = signal(false);

  toggleMenu(): void {
    this.isMenuOpen.update(value => !value);
  }

  closeMenu(): void {
    this.isMenuOpen.set(false);
  }

  readonly menuItems = [
    { label: 'Record Drive', route: '/drives' },
    { label: 'Driving History', route: '/history' },
    { label: 'Costs', route: '/costs' },
    { label: 'Fuel Tracking', route: '/fuel' },
    { label: 'Shareholders', route: '/shareholders' },
    { label: 'Reports', route: '/reports' },
    { label: 'Settlements', route: '/settlements' }
  ];
}

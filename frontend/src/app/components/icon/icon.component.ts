import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type IconName =
  | 'car'
  | 'route'
  | 'coin'
  | 'calc'
  | 'users'
  | 'list'
  | 'plus'
  | 'upload'
  | 'chart'
  | 'right'
  | 'left'
  | 'chevron-down'
  | 'info'
  | 'up'
  | 'down'
  | 'tag'
  | 'globe'
  | 'more'
  | 'alert'
  | 'close';

/**
 * Decorative icon from the sprite in `app.html` (`#i-<name>`). It is always hidden from
 * assistive technology — put the meaning in visible text next to it.
 */
@Component({
  selector: 'app-icon',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<svg class="icon" [class.icon--sm]="small()" aria-hidden="true" focusable="false">
    <use [attr.href]="'#i-' + name()" />
  </svg>`,
  styles: `
    :host {
      display: inline-flex;
      flex: none;
    }
    .icon {
      width: 24px;
      height: 24px;
      fill: none;
      stroke: currentColor;
      stroke-width: 2;
      stroke-linecap: round;
      stroke-linejoin: round;
    }
    .icon--sm {
      width: 20px;
      height: 20px;
    }
  `,
})
export class IconComponent {
  readonly name = input.required<IconName>();
  readonly small = input(false);
}

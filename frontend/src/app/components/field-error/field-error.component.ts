import { Component, computed, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

interface FieldErrorEntry {
  key: string;
  params?: Record<string, unknown>;
}

/** Renders one translated line per active validation error on a control, once it's been touched. */
@Component({
  selector: 'app-field-error',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './field-error.component.html',
  styleUrl: './field-error.component.scss',
})
export class FieldErrorComponent {
  readonly control = input<AbstractControl | null>(null);
  readonly id = input<string | null>(null);

  readonly visible = computed(() => {
    const control = this.control();
    return !!control && control.invalid && control.touched;
  });

  readonly entries = computed<FieldErrorEntry[]>(() => {
    const errors = this.control()?.errors;
    if (!errors) {
      return [];
    }

    const entries: FieldErrorEntry[] = [];
    if (errors['required']) {
      entries.push({ key: 'validation.required' });
    }
    if (errors['maxlength']) {
      entries.push({
        key: 'validation.maxLength',
        params: { max: errors['maxlength'].requiredLength },
      });
    }
    if (errors['minlength']) {
      entries.push({
        key: 'validation.minLength',
        params: { min: errors['minlength'].requiredLength },
      });
    }
    if (errors['min']) {
      entries.push({ key: 'validation.min', params: { min: errors['min'].min } });
    }
    if (errors['pastDate']) {
      entries.push({ key: 'validation.pastDate' });
    }
    return entries;
  });
}

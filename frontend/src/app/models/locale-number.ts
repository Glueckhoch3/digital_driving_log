import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Parse a manually-entered decimal, accepting both `,` and `.` as the decimal
 * separator (e.g. "50,50" and "50.50" both yield 50.5). Manual entries carry no
 * thousands separators, so there is no ambiguity to resolve by language.
 *
 * Returns `null` when the input is empty or not a single valid number.
 */
export function parseLocaleNumber(raw: string | number | null | undefined): number | null {
  if (raw === null || raw === undefined) {
    return null;
  }
  const text = String(raw).trim();
  if (text === '') {
    return null;
  }
  // Normalise the decimal separator, then require a plain decimal number.
  const normalised = text.replace(',', '.');
  if (!/^-?\d*\.?\d+$/.test(normalised)) {
    return null;
  }
  const value = Number(normalised);
  return Number.isFinite(value) ? value : null;
}

/**
 * Validator for text inputs that hold a decimal typed with either separator.
 * Empty is left to `Validators.required`; a present-but-unparseable value is
 * flagged as `{ decimal: true }`, and values below `min` (default 0) as
 * `{ min: { min } }` to match the number-input error the templates already show.
 */
export function localeDecimalValidator(min = 0): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const raw = control.value;
    if (raw === null || raw === undefined || String(raw).trim() === '') {
      return null;
    }
    const value = parseLocaleNumber(raw);
    if (value === null) {
      return { decimal: true };
    }
    return value < min ? { min: { min, actual: value } } : null;
  };
}

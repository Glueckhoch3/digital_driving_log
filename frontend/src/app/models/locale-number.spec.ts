import { FormControl } from '@angular/forms';
import { localeDecimalValidator, parseLocaleNumber } from './locale-number';

describe('parseLocaleNumber', () => {
  it('accepts a dot as the decimal separator', () => {
    expect(parseLocaleNumber('50.50')).toBe(50.5);
  });

  it('accepts a comma as the decimal separator', () => {
    expect(parseLocaleNumber('50,50')).toBe(50.5);
  });

  it('trims surrounding whitespace', () => {
    expect(parseLocaleNumber('  36,89 ')).toBeCloseTo(36.89);
  });

  it('returns null for empty or non-numeric input', () => {
    expect(parseLocaleNumber('')).toBeNull();
    expect(parseLocaleNumber('   ')).toBeNull();
    expect(parseLocaleNumber('abc')).toBeNull();
    expect(parseLocaleNumber('1.2.3')).toBeNull();
    expect(parseLocaleNumber(null)).toBeNull();
  });
});

describe('localeDecimalValidator', () => {
  it('passes a valid decimal typed with either separator', () => {
    expect(localeDecimalValidator(0)(new FormControl('12,5'))).toBeNull();
    expect(localeDecimalValidator(0)(new FormControl('12.5'))).toBeNull();
  });

  it('leaves empty values to the required validator', () => {
    expect(localeDecimalValidator(0)(new FormControl(''))).toBeNull();
  });

  it('flags an unparseable value as a decimal error', () => {
    expect(localeDecimalValidator(0)(new FormControl('1,2,3'))).toEqual({ decimal: true });
  });

  it('flags values below the minimum', () => {
    expect(localeDecimalValidator(0)(new FormControl('-5'))).toEqual({
      min: { min: 0, actual: -5 },
    });
  });
});

import { HttpErrorResponse } from '@angular/common/http';
import { extractApiErrorMessage } from './api-error';

describe('extractApiErrorMessage', () => {
  it('joins field messages when the errors map is present', () => {
    const err = new HttpErrorResponse({
      error: {
        detail: 'Validation failed',
        errors: { name: 'must not be blank', plateNumber: 'too long' },
      },
    });

    expect(extractApiErrorMessage(err, 'fallback.key')).toBe('must not be blank, too long');
  });

  it('returns the detail when only detail is present', () => {
    const err = new HttpErrorResponse({ error: { detail: 'Car not found' } });

    expect(extractApiErrorMessage(err, 'fallback.key')).toBe('Car not found');
  });

  it('falls back to the given key when neither errors nor detail is present', () => {
    const err = new HttpErrorResponse({ error: null });

    expect(extractApiErrorMessage(err, 'fallback.key')).toBe('fallback.key');
  });

  it('falls back to detail when errors is an empty object', () => {
    const err = new HttpErrorResponse({ error: { detail: 'Car not found', errors: {} } });

    expect(extractApiErrorMessage(err, 'fallback.key')).toBe('Car not found');
  });
});

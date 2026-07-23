import { FormControl } from '@angular/forms';
import { pastDateValidator } from './past-date.validator';

describe('pastDateValidator', () => {
  it('allows an empty value', () => {
    const control = new FormControl('', pastDateValidator());
    expect(control.errors).toBeNull();
  });

  it('allows a date in the past', () => {
    const control = new FormControl('2000-01-01', pastDateValidator());
    expect(control.errors).toBeNull();
  });

  it('rejects today', () => {
    const today = new Date().toISOString().slice(0, 10);
    const control = new FormControl(today, pastDateValidator());
    expect(control.errors).toEqual({ pastDate: true });
  });

  it('rejects a future date', () => {
    const future = new Date(Date.now() + 1000 * 60 * 60 * 24 * 365).toISOString().slice(0, 10);
    const control = new FormControl(future, pastDateValidator());
    expect(control.errors).toEqual({ pastDate: true });
  });
});

import { describe, expect, it } from 'vitest';
import { toHttpParams } from './page-query';

describe('toHttpParams', () => {
  it('omits every field when no query is given', () => {
    expect(toHttpParams().keys()).toEqual([]);
  });

  it('omits undefined fields so backend defaults apply', () => {
    const params = toHttpParams({ page: 2 });

    expect(params.get('page')).toBe('2');
    expect(params.has('size')).toBe(false);
    expect(params.has('sort')).toBe(false);
  });

  it('sets a single sort expression', () => {
    expect(toHttpParams({ sort: 'driveDate,desc' }).getAll('sort')).toEqual(['driveDate,desc']);
  });

  it('appends one sort parameter per entry, preserving order', () => {
    const params = toHttpParams({ sort: ['odometer,desc', 'driveDate,desc'] });

    expect(params.getAll('sort')).toEqual(['odometer,desc', 'driveDate,desc']);
  });
});

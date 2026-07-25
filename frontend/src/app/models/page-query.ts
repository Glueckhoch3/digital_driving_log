import { HttpParams } from '@angular/common/http';

/** Optional Spring-style pagination/sort parameters for list endpoints. */
export interface PageQuery {
  /** Zero-based page index. */
  page?: number;
  size?: number;
  /**
   * Sort expression, e.g. `driveDate,desc`. Pass an array for a multi-key sort;
   * each entry becomes its own `sort` parameter, applied in order.
   */
  sort?: string | string[];
}

/** Builds `HttpParams`, omitting any undefined field so backend defaults apply. */
export function toHttpParams(query?: PageQuery): HttpParams {
  let params = new HttpParams();
  if (!query) {
    return params;
  }
  if (query.page !== undefined) {
    params = params.set('page', query.page);
  }
  if (query.size !== undefined) {
    params = params.set('size', query.size);
  }
  if (query.sort !== undefined) {
    for (const sort of Array.isArray(query.sort) ? query.sort : [query.sort]) {
      params = params.append('sort', sort);
    }
  }
  return params;
}

import { HttpParams } from '@angular/common/http';

/** Optional Spring-style pagination/sort parameters for list endpoints. */
export interface PageQuery {
  /** Zero-based page index. */
  page?: number;
  size?: number;
  /** Sort expression, e.g. `driveDate,desc`. */
  sort?: string;
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
    params = params.set('sort', query.sort);
  }
  return params;
}

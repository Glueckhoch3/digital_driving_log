import { HttpErrorResponse } from '@angular/common/http';

export interface ApiProblemDetail {
  detail?: string;
  errors?: Record<string, string>;
}

/**
 * Extracts a user-facing message from a backend ProblemDetail error body,
 * falling back to the given i18n key when the body carries no usable detail
 * (e.g. a network error, where `err.error` isn't a parsed JSON object).
 */
export function extractApiErrorMessage(err: HttpErrorResponse, fallbackKey: string): string {
  const body = err.error as ApiProblemDetail | undefined;
  if (body?.errors && Object.keys(body.errors).length > 0) {
    return Object.values(body.errors).join(', ');
  }
  if (body?.detail) {
    return body.detail;
  }
  return fallbackKey;
}

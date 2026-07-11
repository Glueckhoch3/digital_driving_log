/**
 * Mirrors Spring Data's `Page<T>` JSON envelope returned by the list endpoints.
 * Only the fields the frontend relies on are typed here.
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  /** Zero-based index of the current page. */
  number: number;
  size: number;
}

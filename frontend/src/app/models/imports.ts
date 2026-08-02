/** Number format of an uploaded CSV — mirrors the backend `CsvLocale`. */
export type CsvLocale = 'en' | 'de';

/** A single row that failed CSV validation. `line` is 1-based over the file. */
export interface RowError {
  line: number;
  message: string;
}

/**
 * Result of a CSV import. Imports are all-or-nothing: when `errors` is non-empty
 * nothing was written and `imported` is 0.
 */
export interface ImportResult {
  imported: number;
  errors: RowError[];
}

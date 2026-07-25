import { environment } from '../../environments/environment';

/**
 * Years offered by the year dropdowns in the calculation hub: the current year
 * plus `environment.yearsBack` past ones, newest first.
 *
 * Adjust `yearsBack` in the environment files to widen or narrow the range.
 */
export function selectableYears(yearsBack: number = environment.yearsBack): number[] {
  const current = new Date().getFullYear();
  const years: number[] = [];
  for (let y = current; y >= current - yearsBack; y--) {
    years.push(y);
  }
  return years;
}

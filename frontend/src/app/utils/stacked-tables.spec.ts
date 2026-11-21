import { describe, expect, it } from 'vitest';
import { labelStackedTables } from './stacked-tables';

describe('labelStackedTables', () => {
  it('copies column headings into data-label of body and footer cells', () => {
    const host = document.createElement('div');
    host.innerHTML = `
      <table class="tbl">
        <thead><tr><th> Person </th><th>Betrag</th></tr></thead>
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
        <tfoot><tr><td>Alle</td><td>1</td></tr></tfoot>
      </table>
      <table><thead><tr><th>X</th></tr></thead><tbody><tr><td>y</td></tr></tbody></table>`;

    labelStackedTables(host);

    const cells = host.querySelectorAll('table.tbl td');
    expect(Array.from(cells).map((c) => c.getAttribute('data-label'))).toEqual([
      'Person',
      'Betrag',
      'Person',
      'Betrag',
    ]);
    // tables without the .tbl class are left alone
    expect(host.querySelector('table:not(.tbl) td')?.hasAttribute('data-label')).toBe(false);
  });
});

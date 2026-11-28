/**
 * Keeps `data-label` on the cells of every `table.tbl` in sync with the column headings, so the
 * stacked phone layout (see styles/_base.scss) can print the column name before each value.
 * Headings are translated at runtime, so this watches the DOM instead of using static attributes.
 */
export function labelStackedTables(root: ParentNode): void {
  root.querySelectorAll<HTMLTableElement>('table.tbl').forEach((table) => {
    const headings = Array.from(table.querySelectorAll('thead th')).map(
      (th) => th.textContent?.trim() ?? '',
    );
    table.querySelectorAll('tbody tr, tfoot tr').forEach((row) => {
      Array.from(row.children).forEach((cell, index) => {
        const label = headings[index] ?? '';
        if (cell.getAttribute('data-label') !== label) {
          cell.setAttribute('data-label', label);
        }
      });
    });
  });
}

/** Starts the observer; returns a function that stops it. No-op without a DOM. */
export function watchStackedTables(root: HTMLElement | null): () => void {
  if (!root || typeof MutationObserver === 'undefined') {
    return () => undefined;
  }
  let scheduled = false;
  const run = () => {
    scheduled = false;
    labelStackedTables(root);
  };
  const observer = new MutationObserver((mutations) => {
    // our own attribute writes must not retrigger the run
    if (scheduled || mutations.every((m) => m.type === 'attributes')) {
      return;
    }
    scheduled = true;
    requestAnimationFrame(run);
  });
  observer.observe(root, { childList: true, subtree: true, characterData: true });
  labelStackedTables(root);
  return () => observer.disconnect();
}

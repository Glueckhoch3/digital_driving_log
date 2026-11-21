---
name: ADR-019
title: "UI design system: global tokens (theme Ozean, light + dark), bottom tab bar on phones / top bar on wide screens, stacked tables"
date: 2026-11-18
status: proposed
domain: frontend
agent: Claude Sonnet 5.5
decisionBy: TBD
---

### Context

Issue #22 (improve web design), #41 (settlement shows only the final result) and #43 (easy
language). The old UI had two visual languages (the Calculations area had its own styles, every
other page was almost unstyled), colours were hard-coded hex values per component file, several
pairs failed WCAG AA contrast, the burger menu sat after the title and tables did not reflow on
phones. The review, the principles used and the mockups are in
`docs/plans/WebdesignRedesign.md` (+ `WebdesignRedesign-{A,B,C,D}-*.html`).

### Decision

1. **Global design tokens** in `frontend/src/styles/_tokens.scss` (`:root` CSS variables) are
   the only source of colours, spacing, radii and tap size. Components use `var(--…)`, never raw
   hex. Theme "Ozean" (blue); every text pair ≥ 4.5:1, borders/focus ≥ 3:1.
2. **Dark mode** is part of the token set and follows `prefers-color-scheme`; `data-theme="light|dark"`
   on `<html>` forces a theme (no UI switch yet).
3. **Navigation:** top bar with horizontal links from 48 rem (768 px); below that a fixed bottom
   tab bar (Cars, Log, Settlement, More). Settings pages (people, manage cars, cost types) sit behind
   "Settings" (dropdown on wide screens, bottom sheet on phones). The language button (globe + code)
   is at the right end of the top bar. No burger menu.
4. **Base styles** for bare form controls, buttons and tables live in `styles/_base.scss` inside
   `:where(.main-content)` (zero specificity), so component classes always win without `!important`.
   Shared classes: `.page`, `.card`, `.card-link`, `.btn`, `.msg`, `.seg`, `.badge`, `.tbl`, `.form`.
5. **Tables reflow to stacked rows below 48 rem** (`table.tbl`). The column name before each value is
   written into `data-label` at runtime by `utils/stacked-tables.ts` (needed because headings are
   translated at runtime).
6. **Icons** come from one inline SVG sprite in `app.html`, used through `<app-icon>`; icons are always
   decorative (`aria-hidden`) and accompanied by text.
7. **Wording** follows plain language ("Log", "Fahrt eintragen", "bekommt / zahlt", "Fahrkosten",
   "Feste Kosten"); page description paragraphs were removed. Route `/overview` is now `/log`
   (`/overview` redirects).
8. **Settlement (#41):** `/calculations/combined` shows only "gets / pays" per person (view
   "Result"); the columns paid / variable difference / fixed difference moved to its second view "Details";
   per-car intermediate results stay on `/calculations/results` ("How it was calculated").

### Consequences

- Changing the colour scheme is a one-file change; dark mode needs no per-component work.
- Every new component must use tokens and keep ≥ 48 px touch targets; contrast of new token pairs must be
  re-checked.
- The MutationObserver in `stacked-tables.ts` runs for the whole app; tables that should stack need the
  `tbl` class.
- A manual theme switch, a hamburger-free but richer settings page and further easy-language review with
  users (#43) remain follow-ups.

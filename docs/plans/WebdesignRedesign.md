# Web Design Redesign — Accessible, Mobile-first UI (Issues #22, #41, #43)

**Created:** 2026-11-18
**Status:** In implementation (2026-11-18) — theme **D Ozean** selected; branch `feature/22-webdesign`; see [ADR-019](../../.github/decisions/ADR-019-ui-design-system.md)
**Issues:** [#22 Improve Webdesign](https://github.com/Glueckhoch3/digital_driving_log/issues/22) · [#41 Endabrechnung entschlacken](https://github.com/Glueckhoch3/digital_driving_log/issues/41) · [#43 Leichte Sprache](https://github.com/Glueckhoch3/digital_driving_log/issues/43)
**Scope:** `frontend/src/styles.scss`, `frontend/src/app/**` (header, every page template + `.scss`), `frontend/public/i18n/{en,de}.json`. No backend change.
**Suggested branch:** `feature/22-webdesign`
**Mockups (open in any browser, no build step):**

| Variant | File | Idea |
|---|---|---|
| A — „Klar" | [`WebdesignRedesign-A-Klar.html`](WebdesignRedesign-A-Klar.html) | Top bar, **burger on the left**, drawer menu, light theme, teal accent, card layout |
| B — „Aufgaben" | [`WebdesignRedesign-B-Aufgaben.html`](WebdesignRedesign-B-Aufgaben.html) | **Bottom tab bar** on phones, sidebar on desktop, indigo accent, built-in dark mode, bar chart for the settlement |

### Round 2 — decisions (2026-11-18) and the selected design

Decided after reviewing A and B:

| Topic | Decision |
|---|---|
| Look | **A's look** (top bar, cards, calm light surfaces) |
| Navigation | **B's tab bar**: fixed at the bottom on phones; on wider screens the same items as a horizontal bar at the top (settings behind an "Einstellungen" dropdown instead of the phone's "Mehr" sheet). No burger. |
| Language switch | As in A: globe + "DE" button at the right end of the top bar (on all widths) |
| Naming | **"Log"** in German and English (§4.3 updated accordingly) |
| "Wer zahlt wem?" | **Dropped** — the settlement shows only "bekommt / zahlt" per person (§4.5 updated) |
| Dark mode | **Included** with this change (light + dark token sets, follows `prefers-color-scheme`; manual switch is a later option) |

Two colour themes of this selected layout (identical HTML/CSS apart from the token block, all text pairs ≥ 4.5:1, borders/focus ≥ 3:1, checked for light **and** dark):

| Theme | File | Brand | Notes |
|---|---|---|---|
| **C — Teal** | [`WebdesignRedesign-C-Teal.html`](WebdesignRedesign-C-Teal.html) | `#0f6b62` light / `#5fd0c0` dark | Same colour family as A; calm, "green = go" |
| **D — Ozean** | [`WebdesignRedesign-D-Ozean.html`](WebdesignRedesign-D-Ozean.html) | `#1b5e9b` light / `#7fb8ee` dark | Blue; clearly separate from the red/green money colours, a more neutral/"official" feel |

Both files have Hell/Dunkel/System switches, a width switch (390 / 320 / wide) and the principle notes per screen. Design details specific to this round: the top bar has its own focus-ring colour (`--bar-ink`) because the normal ring colour has no contrast on the coloured bar; the top bar uses a `--bar*` token group so dark mode can use a darker bar; money colours (`--pos`, `--neg`) and the drive/expense badge colours have separate, re-checked dark values; in the real app the tab bar is `position: fixed` (padded with `env(safe-area-inset-bottom)`), in the mockup it is sticky inside the preview frame.

Pick **C or D** (or tweak a colour), then implementation starts (§6). A and B remain as the reference for how the choice was made.

Each mockup has screen tabs (Home, Car, Log, Form, Settlement, Details), a width switch (390 px / 320 px / desktop) and a panel under the preview that lists which principle (P1–P15 below) is used on that screen and why. Demo data is invented. The layouts react to the width of the preview frame (CSS container queries), so they behave like the real responsive app.

> **How this was made.** The current design was reviewed from the code (templates, SCSS, i18n files), not from screenshots of the running app. Findings marked *(verify)* should be checked once in the browser at 360 px width.

---

## 1. What the issues ask for

| Issue | Ask | Where it is answered |
|---|---|---|
| #22.1 | Choose a colour scheme | §4.1 tokens — A teal, B indigo (+ dark) |
| #22.2 | Test mobile accessibility | §6.3 checklist; both variants are designed mobile-first |
| #22.3 | Remove textual page descriptions where possible | §4.4 — rule: no subtitles; help only at the field or in a "?"/note when really needed |
| #22.4 | Align burger menu left | A: burger left. B: no burger — tab bar + "Mehr" (see trade-off in §5) |
| #22.5 | Landing / home pages | Already resolved in #26 — kept: `/` = "Meine Autos" |
| #22.6 | Rename "overview" into "log" | "Log" in both languages (decided in round 2, §4.3) |
| #41 | Final settlement shows only the final debt split; fix/var parts elsewhere | §4.5 — screen "Abrechnung" vs. screen "So wurde gerechnet" |
| #43 | Easy language names, agreed with users | §4.3 — proposed wording table, to be confirmed with users |

---

## 2. Current design — findings

Reviewed: `styles.scss`, `app.scss`, `header.component.{html,scss}`, all page templates/SCSS of the car workspace, management pages, calculations area, `de.json`.

| # | Finding | Evidence | Consequence |
|---|---|---|---|
| F1 | **Two visual languages.** The Calculations area has its own design (`.calc` block with variables, cards, pills, tabs). Every other page uses almost no styling (default browser tables, inputs, plain links). | `styles.scss` `.calc{…}` vs. `drives-list.component.scss`, `drive-form.component.scss` | App feels like several apps; no shared look to build on |
| F2 | **No global design tokens.** Colours are hard-coded hex per file (`#b02020`, `#0a7d32`, `#d9d9d9`, `#2c3e50`, `#3498db`…); `--calc-*` variables exist only under `.calc`. | grep of the SCSS files | Changing the colour scheme (#22.1) means editing ~15 files |
| F3 | **Contrast failures** (WCAG 2.x AA needs 4.5:1 for normal text, 3:1 for large text/UI parts). | white on `#3498db` (active nav) 3.15 · white on `#2980b9` (primary button) 4.30 · `#8b98a5` faint text on white 2.94 · `#16a085` "positive" on white 3.28 · `#e67e22` warning on white 2.85 | Hard to read in sun / for low vision; fails AA |
| F4 | **Header/burger.** Burger comes right after the title instead of at the left edge; the mobile overlay has its own "✕" while the burger stays; overlay `top:60px` is hard-coded and may not match the header height; burger hit area ≈ 41 px; no focus style. *(verify)* | `header.component.scss` | Issue #22.4; small tap target; two competing close controls |
| F5 | **Flat navigation with developer wording.** Six equal items: "Managementübersicht", "Auto auswählen", "Berechnungen", "Benutzer verwalten", "Autos verwalten", "Kostenarten verwalten". Daily tasks and admin tasks are mixed. | `header.component.ts` | Hick's law (too many equal choices); unclear names (#22.6, #43) |
| F6 | **Page descriptions.** Subtitle/description texts on the Calculations pages (`calc__sub`), user/car management (`description`), settlement `explainer` paragraphs. | `de.json` keys `*.subtitle`, `*.description`, `*.explainer` | Issue #22.3; text the user must read before acting |
| F7 | **Wide tables on phones.** Costs list has 7 columns, drives list 4, settlement 7 — no reflow, only `overflow-x:auto` in `.calc`. *(verify)* | `costs-list.component.html`, `yearly-settlement.component.html` | Sideways scrolling on 360 px; violates WCAG 1.4.10 Reflow |
| F8 | **Forms are unstyled.** Inputs/labels get only `width:100%; margin-top`; no custom focus ring, default control sizes (≈ 21–30 px tall), "Back" is a plain text link. | `drive-form.component.scss` | Small touch targets, weak hierarchy, inconsistent with `.calc__control` |
| F9 | **Settlement mixes levels.** The yearly table shows distance, both factors, both owed parts and the total in one 7-column row, plus a paragraph of explanation. | `yearly-settlement.component.html` | Issue #41: the one number users care about is lost |
| F10 | **Technical wording in the UI**: "Aggregieren", "Nettosaldo", "Beteiligte", "Fix./Var. Faktor", "Distanz", "Odometer". | `de.json` | Issue #43 |
| F11 | **Good already** (keep): semantic `<table>`/`scope`, `aria-live` status/error regions, `aria-invalid` + `aria-describedby` on fields, i18n in two languages, routerLinkActive, i18n-ed pager. | templates | The redesign is mostly visual + structural, the accessibility *code* base is a good start |

---

## 3. Design principles used

Numbered so the mockups and later PRs can reference them ("P7"). Each has a short *what / why / how we apply it* and where to read more.

| ID | Principle | What it means | How it is applied here |
|---|---|---|---|
| **P1** | **Visual hierarchy** | Size, weight, colour and position tell the eye what matters first. One primary action per screen. | Page title largest; the settlement amount is the largest thing in its card; "Fahrt eintragen" is the only filled button on the car page |
| **P2** | **Consistency & design tokens** | Same thing looks and behaves the same everywhere. Define colours, spacing, radii once as variables ("tokens"). | One `:root` token set (§4.1) replaces per-file hex values and the `--calc-*` set |
| **P3** | **Proximity & grouping** (Gestalt) | Things that belong together sit close together and are separated from other groups by space or lines. | "Eintragen" (do) vs. "Ansehen" (look) groups; entries in the log grouped by day; menu split into *Täglich* / *Einstellungen* |
| **P4** | **Contrast, and meaning not by colour alone** (WCAG 1.4.3, 1.4.11, 1.4.1) | Text ≥ 4.5:1, UI parts ≥ 3:1. Never use only colour to say something (colour-blindness). | All pairs validated (§4.1). "bekommt/zahlt" = word + arrow + colour; log type = word + icon + colour; errors = icon + text + border |
| **P5** | **Large touch targets** (WCAG 2.5.8 min. 24 px; Apple 44 pt, Material 48 dp) | Fingers are imprecise. | Every control ≥ 48 px high (variable `--tap`), 8 px gaps between neighbours |
| **P6** | **Recognition over recall; visible labels** (Nielsen heuristic 6) | Show the choices; do not make people remember. Labels stay visible (no placeholder-only fields). | Icon **and** text everywhere; label above every field; active menu item marked |
| **P7** | **Progressive disclosure** | Show the essentials first; details one step away. | Settlement → final result only; "So wurde gerechnet" holds fix/var parts (#41). Page descriptions removed (#22.3) |
| **P8** | **Mobile-first & reflow** (WCAG 1.4.10) | Design for the smallest screen, enhance for larger. No sideways scrolling at 320 px. | Tables turn into stacked rows/cards; grids 1 → 2 → 3 columns by available width |
| **P9** | **Plain language** (WCAG 3.1.5, Leichte Sprache) | Short, everyday words; same word for same thing; verbs for actions. | §4.3 table; "Fahrt eintragen", "bekommt / zahlt" |
| **P10** | **Visibility of system status & feedback** (Nielsen heuristic 1) | Always show loading, success, error, empty. | Status banners with `role="status"/"alert"` kept; error pattern defined once (§4.2) |
| **P11** | **Thumb zone / Fitts's law** | Targets that are big and close to the thumb are fast to hit. | Variant B: tab bar and "Speichern" at the bottom edge. Variant A: burger at the left edge as asked |
| **P12** | **Typography & white space** | 16 px+ body, 1.5 line height, a limited type scale, an 4/8-based spacing scale. | Scale in §4.1; tabular numbers for money |
| **P13** | **Focus & keyboard** (WCAG 2.4.7, 2.1.1) | Every control reachable and visibly focused. | 3 px focus ring token; native `<button>`, `<a>`, `<details>`; Esc closes menu/sheet |
| **P14** | **Fewer choices at once** (Hick's law) | More options → slower decisions. | Max. 4 main navigation targets; rare ones under "Mehr"/"Einstellungen"; filters with ≤ 5 options |
| **P15** | **Icon + text together** | Icons alone are ambiguous. | Icons are `aria-hidden`, text always present |

Further reading: WCAG 2.2 quick reference (w3.org/WAI/WCAG22/quickref), Nielsen's 10 usability heuristics (nngroup.com), Material 3 "Navigation bar / Navigation rail" guidance, Laws of UX (lawsofux.com), "Leichte Sprache" rules (Netzwerk Leichte Sprache).

---

## 4. Shared foundation (both variants)

### 4.1 Tokens

Defined once in `styles.scss` under `:root` (and, for B, again under `@media (prefers-color-scheme: dark)` / `[data-theme]`). Components only use tokens (P2).

| Token | A „Klar" (light) | B „Aufgaben" light | B dark |
|---|---|---|---|
| `--bg` page | `#f5f7f6` | `#f4f6fb` | `#121829` |
| `--surface` cards | `#ffffff` | `#ffffff` | `#1b2238` |
| `--ink` text | `#1b2a2e` | `#16203a` | `#e8ecf8` |
| `--muted` secondary text | `#52625f` | `#505b75` | `#a3adc9` |
| `--line` borders | `#d5dedb` | `#d9deec` | `#2d3756` |
| `--brand` (buttons, active) | `#0f6b62` | `#2a4bd7` | `#8fa8ff` |
| `--brand-ink` text on brand | `#ffffff` | `#ffffff` | `#0e1426` |
| `--pos` / `--pos-soft` (gets money) | `#14703f` / `#e8f5ee` | `#126b3a` / `#e5f5ea` | `#6fdc9a` / `#1a2a22` |
| `--neg` / `--neg-soft` (pays / error) | `#a52a2a` / `#fbeaea` | `#a3262a` / `#fdeaea` | `#ff9a9d` / `#2c1b1d` |
| `--cost` / `--cost-soft` (expense) | `#7a4a00` / `#fdf1d9` | `#7a4a00` / `#fdf1d9` | `#f2c36b` / `#3a2f17` |
| `--focus` ring | `#1d4ed8` | `#1d4ed8` | `#b8c8ff` |

**Measured contrast (WCAG ratio, needs ≥ 4.5 for text):**

| Pair | A | B light | B dark |
|---|---|---|---|
| ink on bg | 13.8 | 14.9 | 15.0 |
| muted on bg | 6.0 | 6.3 | 7.9 |
| brand-ink on brand | 6.4 | 6.8 | 8.0 |
| pos on pos-soft | 5.5 | 5.8 | 8.9 |
| neg on neg-soft | 6.1 | 6.3 | 8.1 |
| brand text on surface | 6.4 | 6.8 | 8.8 |

(Today for comparison: white on `#3498db` 3.15, faint grey `#8b98a5` 2.94 — see F3.) Re-check with a tool (browser DevTools → "Contrast") after implementing, as soon as colours are tweaked.

Other tokens: spacing `4/8/12/16/24/32/48 px` (`--s1…--s7`), radius `10–16 px`, tap size `--tap: 48px`, type scale `14 / 16 / 17-18 / 24-28 px` (body never below 16 px), font stack stays the system font (no webfont download, faster and GDPR-friendly).

### 4.2 Shared patterns

- **Page skeleton:** title (h2) → optional segmented control/filter → content. No subtitle (P7).
- **Buttons:** one filled (primary) per screen, others outlined; 48 px high; label is a verb.
- **Lists/tables:** on wide containers a real `<table>`; below ~760 px each row becomes a stacked block with the column name in front of the value (`data-label` pattern, A) or a list row (B). Container queries, not viewport media queries, so components also work in narrow columns.
- **Forms:** visible label above field, short hint under the label, 48–52 px fields, 16 px text (stops iOS zoom), error = red border + icon + sentence, linked by `aria-describedby`; actions at the end (B: sticky at the bottom).
- **Status:** `role="status"` (success) / `role="alert"` (error); empty states say what to do next in one sentence.
- **Focus:** one global `:focus-visible` rule using `--focus`.
- **Skip link** "Zum Inhalt springen" as first element in `app.html` (not in mockups).

### 4.3 Navigation & easy-language naming (#22.6, #43)

These are **proposals**; #43 says to agree the wording with users — treat as the starting list for that conversation.

| Today (DE) | Proposed (DE) | Proposed (EN) | Note |
|---|---|---|---|
| Managementübersicht | **Log** | **Log** | #22.6; decided: "Log" in both languages (round 2) |
| Auto auswählen | **Meine Autos** | My cars | Home `/` |
| Berechnungen | **Abrechnung** | Settlement | Who owes what |
| Benutzer verwalten | **Personen** | People | Under "Einstellungen" / "Mehr" |
| Autos verwalten | **Autos verwalten** | Manage cars | Settings |
| Kostenarten verwalten | **Kostenarten** | Cost types | Settings |
| Fahrt hinzufügen | **Fahrt eintragen** | Add trip | Verb people use for a log book |
| Kosten hinzufügen / ansehen | **Ausgabe eintragen** / **Ausgaben** | Add expense / Expenses | "Kosten" is ambiguous (fix vs. variable) |
| CSV importieren | **Datei hochladen** | Upload file | |
| Verbrauchsstatistik | **Verbrauch** | Consumption | |
| Odometer / Kilometerstand | **Kilometerstand** + hint "Die Zahl auf dem Tacho nach der Fahrt." | Odometer reading | |
| Fahrer | **Wer ist gefahren?** | Who drove? | question labels read naturally |
| Nettosaldo | **bekommt / zahlt** | gets / pays | |
| Gesamtabrechnung / Endabrechnung | **Ergebnis für alle Autos** | Result for all cars | |
| Variable Kosten / Var. Faktor | **Fahrkosten** (z. B. Sprit) | Driving costs | split by kilometres (ADR-015) |
| Fixe Kosten / Fix. Faktor | **Feste Kosten** (z. B. Versicherung) | Fixed costs | split equally (ADR-015) |
| Aggregieren (Monat) | **Monat zusammenrechnen** | Add up month | |
| Beteiligte | **Wer zahlt mit?** | Who shares the costs? | |
| Berechnung starten / Jahr berechnen | **Jahr abrechnen** | Settle the year | |

Rules: one word per concept everywhere (also in error messages and CSV import text); verbs for buttons; no abbreviations (except initials in names, which come from the data); numbers/units always with unit ("48.230 km", "62,40 €"). All changes go into **both** `de.json` and `en.json`.

### 4.4 Removing descriptions (#22.3)

Rule of thumb: *if the title + the controls already say it, delete the text.*

| Keep | Remove / move |
|---|---|
| Hint at the field when the value is ambiguous ("Die Zahl auf dem Tacho…") | `*.subtitle` on Calculations pages, `userManagement.description`, `carManagement.description` |
| One-sentence empty state with next step | Long `calculations.*.explainer` paragraphs → shrink to one two-line note on the "So wurde gerechnet" page (see mockups) |
| Error/success messages | Page intro sentences |

### 4.5 Settlement split (#41)

Two screens instead of one dense table:

1. **Abrechnung** (final result) — year selector, one card (A) or bar (B) per person: **bekommt/zahlt X €** with arrow + word + colour. One link: "So wurde gerechnet". (The optional "Wer zahlt wem?" transfer list was **dropped** in round 2.)
2. **So wurde gerechnet** (intermediate results) — the existing tabs *Pro Person* (km, Fahrkosten, Feste Kosten, Zusammen), *Kilometer pro Monat*, *Anteile* (factors). Fix/Var shares live here. One two-line note explains the split rule.

Existing routes map cleanly: `/calculations/combined` → "Abrechnung", `/calculations/results` (tabs yearly / monthly / factors) → "So wurde gerechnet"; the run/participants actions stay on the calculations hub (renamed "Abrechnung vorbereiten"; out of scope to redesign deeper).

---

## 5. The two variants

### Variant A — „Klar" (top bar, burger left, cards)

- **Navigation:** green top bar; on phones a **burger at the far left** opens a left drawer (grouped *Täglich* / *Einstellungen*, scrim, Esc closes); on desktop the same three main links sit inline in the bar and the burger disappears.
- **Look:** light, calm, lots of white cards on a pale grey-green page; teal brand colour; 12 px radius.
- **Settlement:** one card per person with initials avatar and amount; "Wer zahlt wem?" as pair cards.
- **Pros:** closest to the current structure → least migration risk; answers #22.4 literally; one theme to maintain; familiar pattern.
- **Cons:** primary navigation sits at the top (hardest zone to reach one-handed on big phones); burger hides navigation (lower discoverability).

### Variant B — „Aufgaben" (bottom tabs, sidebar on desktop, dark mode)

- **Navigation:** phones get a **bottom tab bar** (Autos · Verlauf · Abrechnung · Mehr); "Mehr" opens a bottom sheet with settings and language. From ~760 px the bar becomes a **left sidebar** with the same items. No burger at all.
- **Look:** indigo accent, bigger radii (16 px), list rows instead of cards, round coloured icons for drive/expense, **dark mode** via `prefers-color-scheme` (+ manual switch in the mockup).
- **Settlement:** one diverging bar chart (left = pays, right = gets) with text labels, then transfer list; details as expandable rows (`<details>`).
- **Pros:** best for one-handed phone use (P11) and for "do a task quickly" users; navigation always visible (P6); no layout shift between pages; dark mode for night driving.
- **Cons:** needs a new app-shell (sidebar/tab component) → more work; deviates from #22.4 (no burger — but the intent, "reachable and consistent menu", is met); dark theme doubles the contrast checking.

### Quick comparison

| | A „Klar" | B „Aufgaben" |
|---|---|---|
| Implements #22.4 literally | ✅ | ➖ (replaced by tab bar) |
| One-handed phone use | ➖ | ✅ |
| Navigation discoverability | ➖ (hidden in burger) | ✅ (always visible) |
| Effort | lower (header + tokens + restyle) | higher (new shell, sheet, dark tokens, chart) |
| Dark mode | later, optional | included |
| Risk | low | medium |

**Recommendation:** start with **A** (it satisfies every checkbox of #22 and is the smaller change), but build the tokens (§4.1) and the list/table reflow so they are theme-agnostic — then B's tab bar and dark theme can be added later as an increment without touching pages again. If most users are on phones and mainly *enter* data, take **B**. A hybrid is possible: A's look + B's bottom tab bar on phones.

---

## 6. Implementation plan

### 6.1 Steps (small PRs into `dev`, branch `feature/22-webdesign`)

1. **Tokens & base** — `:root` tokens, base typography, global `:focus-visible`, skip link, `button`/`input`/`select`/`textarea`/table base styles; fold `.calc` variables into the global tokens; remove hard-coded hex in component SCSS.
2. **App shell** — rebuild `header` (A: burger left + left drawer, Esc, focus trap, `aria-expanded`/`aria-controls`; B: `app-shell` with tab bar/sidebar + "Mehr" sheet); fix the mobile overlay height issue (F4).
3. **Shared components** — `app-page-title`, `app-button` styles (or utility classes), `app-list-row`/stacked-table pattern, `app-segmented`, `app-status` (success/error/empty).
4. **Pages, in order of use:** car selection → car hub → drive form / cost form → drives & costs lists → log (rename) → upload → statistics → management pages → calculations (#41 split).
5. **Wording** (#43): update `de.json` + `en.json` together (§4.3), review with users.
6. **Remove descriptions** (#22.3): delete the keys that become unused (keep both languages in sync).
7. **Docs:** update `.claude/PROJECT_MAP.md` (routes/labels), add an **ADR** (next free number, e.g. ADR-019: "UI design system: tokens, navigation pattern, container-query reflow") with the chosen variant; update `docs/documentation.md` screenshots if present.

### 6.2 Files touched (expected)

`styles.scss` · `app.html` / `app.scss` · `components/header/*` (+ new shell components for B) · every `pages/**/*.html|scss` · `public/i18n/{en,de}.json` · specs that assert on translated labels / burger behaviour (`header`, `calculations/*.spec.ts`).

### 6.3 Mobile & accessibility test checklist (#22.2)

Do this at the end of every step, on **360 × 800** and **320 × 640**, light and (B) dark:

- [ ] No horizontal page scroll (WCAG 1.4.10); text still readable at browser zoom 200 %.
- [ ] Every button/link/field ≥ 44–48 px high, ≥ 8 px apart.
- [ ] Keyboard only: Tab order logical, focus always visible, Esc closes menu/sheet, no focus trap left behind.
- [ ] Screen reader (NVDA/VoiceOver/TalkBack) reads page title, nav landmarks, form labels, errors (`role="alert"`), the "bekommt/zahlt" words.
- [ ] Contrast checked in DevTools for every token pair used; nothing relies on colour alone.
- [ ] Browser DevTools → Lighthouse accessibility ≥ 95; `axe` (browser extension) shows 0 serious issues.
- [ ] Real phone test (iOS Safari + Android Chrome): form inputs do not zoom (≥ 16 px), date picker works, thumb reach for primary action.
- [ ] Language switch DE/EN: no truncation, no leftover untranslated key.
- [ ] `prefers-reduced-motion`: drawer/sheet transitions are disabled or very short.

### 6.4 Decisions needed from you

1. ~~Variant~~ → hybrid of A + B (decided, §Round 2).
2. ~~Name of the overview~~ → "Log" (decided).
3. ~~"Wer zahlt wem?" list~~ → dropped (decided).
4. Which users to ask for the Leichte-Sprache wording (#43), and is the §4.3 table a good starting list?
5. ~~Dark mode~~ → included (decided).
6. Colour theme: **C Teal** or **D Ozean**?

### 6.5 Out of scope

Backend/API changes, a custom logo/brand mark, webfonts, redesigning the participants/run workflows beyond restyling, print stylesheets, charts beyond the settlement bars.

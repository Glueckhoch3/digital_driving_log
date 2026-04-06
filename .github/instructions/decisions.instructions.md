---
name: adr.instructions
description: "Use when: writing, updating, or reading architectural decision records. Use when starting any non-trivial task to check existing decisions."
applyTo: ".github/decisions/**"
visibility: public
---

**Maintainer:** Team  
**Purpose:** Template, rules, and workflow for architectural decision records.

## When to write an ADR

Write one when you:

- Choose between two or more real implementation approaches
- Add a new dependency to the project
- Define a data model, API contract, or global pattern from scratch
- Make a decision a future agent might unknowingly reverse

Skip it when you:

- Follow a pattern already documented in an existing ADR
- Make a trivially reversible change with no downstream effects
- Apply a style rule already covered in the instruction files

## Workflow

1. Read `decisions/_index.md` — check if a decision already covers your situation
2. If writing a new ADR, pick the next ID from the index
3. Create `decisions/ADR-NNN-short-title.md` using the template below
4. Add a row to `decisions/_index.md` in the same commit
5. If this supersedes an existing ADR, update that file's `supersededBy` field

## Status values

- `proposed` — written, awaiting human review
- `accepted` — in effect; agents must follow this
- `superseded` — replaced by a newer ADR (link it)
- `deprecated` — no longer relevant

Agents must never silently overwrite an accepted decision.
Create a new ADR and mark the old one superseded instead.

## Template

---

name: ADR-NNN
title: ""
date: YYYY-MM-DD
status: proposed
domain: backend | frontend | fullstack | infra
agent: <agent-name>
decisionBy: agent | team | <name>
supersedes: —
supersededBy: —

---

### Context

What situation forced this decision? What constraint or open question existed?
(2–4 sentences)

### Decision

What was decided? Lead with one clear sentence.

### Alternatives considered

- **Option A** — why it was on the table, why it lost
- **Option B** — same

### Consequences

- ✅ What gets better
- ⚠️ What becomes a constraint
- ❌ What you're giving up

### Affected files / areas

- list paths or modules
---
name: project.instructions
description: "Use when: project-wide decisions, contribution rules, code style, and PR guidance."
applyTo: "**"
visibility: public
---

**Maintainer:** Team
**Purpose:** Project-wide conventions, contribution flow, release and CI guidance for contributors and the AI assistant.

## Scope
- Applies to all folders and cross-cutting project policies. Use file-specific instructions for folder-scoped rules (e.g., `backend/**`, `frontend/**`).

## Contribution Flow
- Branch naming: use `feature/<short-description>`, `fix/<short-description>`, `chore/<short-description>`.
- Keep PRs small and focused. Each PR should include a brief description, relevant screenshots (if UI), and a checklist for migrations or DB changes.

## Commit Messages
- Use imperative tense and reference issue IDs where applicable: `Add: validate drive distance (#123)`.

## CI & Releases
- CI runs on push and PRs. Make sure local checks pass before pushing.
- Tag releases with semantic versions and include a short changelog entry.

## Security & Secrets
- Never commit secrets or credentials. Use environment variables and document secret requirements in the `.env.example`.

## Documentation
- Update `README.md` and `documentation.md` for any user-facing changes or large architecture changes.

## Maintainer Decisions
- Use folder-scoped instruction files for implementation-specific rules. Record architecture decisions in `.github/decisions/`.


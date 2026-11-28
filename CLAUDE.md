# Digital Driving Log

Github Repository: https://github.com/Glueckhoch3/digital_driving_log/tree/dev

## Branching
- `main` and `dev` are protected. Never commit directly to them.
- Make all changes on a feature branch (`feature/…`, `fix/…`, `chore/…`) and merge it into `dev` via pull request.

## GitHub issues
Use the `gh` CLI (already allowed in `.claude/settings.local.json` as `gh issue *`), run from the repo root:
- List open issues: `gh issue list`
- Read one issue with comments: `gh issue view <number> --comments`
- Filter/search: `gh issue list --label <label> --state all --search "<text>"`
- Machine-readable: add `--json number,title,body,labels,state`

Reference issues in commits/PRs by number (e.g. `(#28)`).

Project layout, endpoints, conventions and gotchas:

@.claude/PROJECT_MAP.md

Before changing code, read the matching rules in `.github/instructions/` (backend, frontend, model, project) and record architecture decisions as ADRs in `.github/decisions/`.

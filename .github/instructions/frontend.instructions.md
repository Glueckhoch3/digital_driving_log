---
name: frontend.instructions
description: "Use when: frontend-wide decisions, contribution rules, code style, and PR guidance."
applyTo: "frontend/**"
visibility: public
---

**Maintainer:** Team
**Purpose:** Frontend conventions, component patterns, and contribution guidance for the AI assistant.

# Frontend Development Instructions

## Framework
- This project uses Angular 21. Prefer idiomatic Angular 21 APIs and patterns while keeping compatibility with the existing codebase.
- SCSS is the preferred styling language.

## Component Structure
- Keep components split into separate files: `.ts` (or `.tsx` when applicable), `.html`, `.scss`.
- Prefer standalone components by default and use `inject()` for constructor-less DI when appropriate.

## Reactivity & Templates
- Prefer Signals for reactive state management where it improves clarity and performance.
- Use the modern component APIs (`input()`, `output()`, `model()`) and function-based guards/interceptors when it fits the use case.
- Structural directives (`*ngIf`, `*ngFor`, `*ngSwitch`) remain acceptable where they are the simplest solution; prefer signal-driven patterns and template helpers when those produce clearer code.

## Template Practices
- Keep templates declarative and small. Move complex logic to the component class or small pure utility functions.
- Prefer readable bindings over nested expressions. Favor `trackBy` for `ngFor` lists with dynamic content.

## Code Style
- Use TypeScript strict mode and follow Angular style guide conventions.
- Use meaningful variable and function names and keep components focused (single responsibility).

## Tooling & Testing
- Keep the `package.json` scripts minimal and document any dev-only tools.
- Add unit tests for component logic (signals, selectors, pipes) and shallow template tests for important UI behaviours.

## Accessibility & i18n
- Respect existing `public/i18n/` JSON bundles. Add translation keys for new UI text.
- Ensure components are accessible: semantic markup, ARIA when needed, keyboard navigation.

## PRs
- Describe UI changes, include screenshots for visual updates and list i18n keys added/changed.

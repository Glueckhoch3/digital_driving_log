# Frontend Development Instructions

## Framework
- Angular 21 is used in this project
- Always use the latest Angular 21 syntax and features
- SCSS is the preferred styling language

## Template Syntax
- Always split components into separate files: `.ts`, `.html`, `.scss`
- Use Angular's built-in directives and bindings for templates
- Use control flow syntax: `@if`, `@else`, `@for`, `@switch`
- When determining template renders based on conditions (e.g `@if`, `@else` etc.) prefer using signals to determine the state
- **Do NOT** use legacy directives: `*ngIf`, `*ngFor`, `*ngSwitch`

## Angular 21 Features
- Use standalone components by default
- Use `inject()` function for dependency injection in constructor-less components
- Use signals for reactive state management when appropriate
- Use the new `input()`, `output()`, `model()` functions for component APIs
- Prefer function-based guards and interceptors over class-based ones

## Code Style
- Use TypeScript strict mode
- Follow Angular style guide
- Use meaningful variable and function names
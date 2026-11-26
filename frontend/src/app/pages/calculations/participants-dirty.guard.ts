import { CanDeactivateFn } from '@angular/router';
import { CalculationRunComponent } from './calculation-run.component';

/** Blocks leaving the run page with unsaved participant checkbox edits (issue #32, F3). */
export const participantsDirtyGuard: CanDeactivateFn<CalculationRunComponent> = (component) =>
  component.canDeactivate();

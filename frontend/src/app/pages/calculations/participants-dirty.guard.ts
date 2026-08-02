import { CanDeactivateFn } from '@angular/router';
import { CalculationHubComponent } from './calculation-hub.component';

/** Blocks leaving the hub with unsaved participant checkbox edits (issue #32, F3). */
export const participantsDirtyGuard: CanDeactivateFn<CalculationHubComponent> = (component) =>
  component.canDeactivate();

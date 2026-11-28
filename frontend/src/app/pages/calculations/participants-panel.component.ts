import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { CalculationService } from '../../services/calculation.service';
import { ParticipantRow } from '../../models/calculations';
import { AvailabilityStore } from './availability.store';

type Message = {
  type: 'ok' | 'error' | 'warn';
  key: string;
  params?: Record<string, unknown>;
} | null;

/** Row-level preview used while the user is still ticking checkboxes, before saving. */
function allocate(
  total: number,
  ids: number[],
  weightOf: (id: number) => number,
): Map<number, number> {
  const out = new Map<number, number>();
  if (ids.length === 0) return out;
  const totalWeight = ids.reduce((sum, id) => sum + weightOf(id), 0);
  let running = 0;
  ids.forEach((id, i) => {
    let share: number;
    if (i === ids.length - 1) {
      share = Math.round((total - running) * 100) / 100;
    } else if (totalWeight === 0) {
      share = Math.round((total / ids.length) * 100) / 100;
    } else {
      share = Math.round(((total * weightOf(id)) / totalWeight) * 100) / 100;
    }
    out.set(id, share);
    running += share;
  });
  return out;
}

/**
 * Who shares one car-year's costs (issue #32), shown on the run page above the
 * calculation actions (issue #41).
 */
@Component({
  selector: 'app-participants-panel',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './participants-panel.component.html',
})
export class ParticipantsPanelComponent {
  private readonly calculationService = inject(CalculationService);
  private readonly translate = inject(TranslateService);
  readonly store = inject(AvailabilityStore);

  readonly carId = input.required<number>();
  readonly year = input.required<number>();

  readonly rows = signal<ParticipantRow[]>([]);
  readonly loading = signal(false);
  readonly message = signal<Message>(null);
  readonly busy = signal(false);

  /** The checkbox state as the user edits it — separate from the last-loaded set. */
  readonly checkedIds = signal<Set<number>>(new Set());
  private readonly savedIds = signal(new Set<number>());
  private request?: Subscription;

  readonly dirty = computed(() => !setsEqual(this.checkedIds(), this.savedIds()));

  readonly preview = computed(() => {
    const ids = [...this.checkedIds()].sort((a, b) => a - b);
    const distanceOf = (id: number) => this.rows().find((r) => r.userId === id)?.distance ?? 0;
    const fix = allocate(100, ids, () => 1);
    const varr = allocate(100, ids, distanceOf);
    return { fix, varr };
  });

  readonly yearCalculated = computed(() => this.store.yearCalculated(this.year()));

  constructor() {
    // a new car/year starts clean: nothing from the previous selection carries over
    effect(() => {
      const carId = this.carId();
      const year = this.year();
      untracked(() => {
        this.rows.set([]);
        this.checkedIds.set(new Set());
        this.savedIds.set(new Set());
        this.message.set(null);
        if (carId) this.load(carId, year);
      });
    });
  }

  /** Re-reads distances after a calculation action, keeping unsaved ticks. */
  reload(): void {
    this.load(this.carId(), this.year());
  }

  private load(carId: number, year: number): void {
    this.request?.unsubscribe();
    this.loading.set(true);
    this.request = this.calculationService.getParticipants(carId, year).subscribe({
      next: (set) => {
        const ids = new Set(set.rows.filter((r) => r.participating).map((r) => r.userId));
        // drivers are always in, so unsaved edits only add to what the server reports for them
        this.checkedIds.set(
          this.dirty()
            ? new Set([
                ...this.checkedIds(),
                ...set.rows.filter((r) => r.hasDrives).map((r) => r.userId),
              ])
            : ids,
        );
        this.savedIds.set(ids);
        this.rows.set(set.rows);
        this.loading.set(false);
      },
      error: () => {
        this.rows.set([]);
        this.checkedIds.set(new Set());
        this.savedIds.set(new Set());
        this.loading.set(false);
      },
    });
  }

  toggle(row: ParticipantRow): void {
    if (row.hasDrives) return; // drivers can't be removed
    const next = new Set(this.checkedIds());
    if (next.has(row.userId)) {
      next.delete(row.userId);
    } else {
      next.add(row.userId);
    }
    this.checkedIds.set(next);
  }

  save(): void {
    this.message.set(null);
    this.busy.set(true);
    this.calculationService
      .saveParticipants({ carId: this.carId(), year: this.year(), userIds: [...this.checkedIds()] })
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.savedIds.set(new Set(this.checkedIds()));
          this.store.refresh();
          this.message.set(
            this.yearCalculated()
              ? {
                  type: 'warn',
                  key: 'calculations.participants.savedStaleRun',
                  params: { year: this.year() },
                }
              : { type: 'ok', key: 'calculations.participants.saved' },
          );
          this.reload();
        },
        error: (err) => {
          this.busy.set(false);
          this.message.set({
            type: 'error',
            key:
              err?.error?.detail === 'participants.driverRequired'
                ? 'calculations.participants.driverRequired'
                : 'calculations.messages.actionFailed',
          });
        },
      });
  }

  reset(): void {
    if (!window.confirm(this.translate.instant('calculations.participants.resetConfirm'))) return;
    this.message.set(null);
    this.busy.set(true);
    this.calculationService.deleteParticipants(this.carId(), this.year()).subscribe({
      next: () => {
        this.busy.set(false);
        this.store.refresh();
        this.checkedIds.set(new Set(this.savedIds()));
        this.reload();
      },
      error: () => {
        this.busy.set(false);
        this.message.set({ type: 'error', key: 'calculations.messages.actionFailed' });
      },
    });
  }

  confirmDiscard(): boolean {
    if (!this.dirty()) return true;
    return window.confirm(this.translate.instant('calculations.participants.unsavedConfirm'));
  }
}

function setsEqual(a: Set<number>, b: Set<number>): boolean {
  if (a.size !== b.size) return false;
  for (const id of a) if (!b.has(id)) return false;
  return true;
}

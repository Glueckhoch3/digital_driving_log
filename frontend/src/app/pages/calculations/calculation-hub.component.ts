import { Component, computed, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CalculationService } from '../../services/calculation.service';
import { ParticipantRow } from '../../models/calculations';
import { AvailabilityStore } from './availability.store';
import { CalcPeriodSelectComponent } from './calc-period-select.component';

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

@Component({
  selector: 'app-calculation-hub',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslateModule, CalcPeriodSelectComponent],
  templateUrl: './calculation-hub.component.html',
})
export class CalculationHubComponent {
  private readonly calculationService = inject(CalculationService);
  private readonly translate = inject(TranslateService);
  readonly store = inject(AvailabilityStore);

  readonly carId = signal(0);
  readonly year = signal(new Date().getFullYear());

  readonly rows = signal<ParticipantRow[]>([]);
  readonly loading = signal(false);
  readonly message = signal<Message>(null);
  readonly busy = signal(false);

  /** The checkbox state as the user edits it — separate from the last-loaded set. */
  readonly checkedIds = signal<Set<number>>(new Set());
  private savedIds = new Set<number>();

  readonly dirty = computed(() => !setsEqual(this.checkedIds(), this.savedIds));

  readonly preview = computed(() => {
    const ids = [...this.checkedIds()].sort((a, b) => a - b);
    const distanceOf = (id: number) => this.rows().find((r) => r.userId === id)?.distance ?? 0;
    const fix = allocate(100, ids, () => 1);
    const varr = allocate(100, ids, distanceOf);
    return { fix, varr };
  });

  readonly yearCalculated = computed(() => this.store.yearCalculated(this.year()));

  constructor() {
    effect(() => {
      const carId = this.carId();
      const year = this.year();
      if (!carId) return;
      this.loadParticipants(carId, year);
    });
  }

  private loadParticipants(carId: number, year: number): void {
    this.loading.set(true);
    this.calculationService.getParticipants(carId, year).subscribe({
      next: (set) => {
        this.rows.set(set.rows);
        const ids = new Set(set.rows.filter((r) => r.participating).map((r) => r.userId));
        this.checkedIds.set(ids);
        this.savedIds = new Set(ids);
        this.loading.set(false);
      },
      error: () => {
        this.rows.set([]);
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
          this.savedIds = new Set(this.checkedIds());
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
          this.loadParticipants(this.carId(), this.year());
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
        this.loadParticipants(this.carId(), this.year());
      },
      error: () => {
        this.busy.set(false);
        this.message.set({ type: 'error', key: 'calculations.messages.actionFailed' });
      },
    });
  }

  deleteCalculation(): void {
    if (!window.confirm(this.translate.instant('calculations.participants.deleteRunConfirm')))
      return;
    this.message.set(null);
    this.busy.set(true);
    this.calculationService.deleteYear(this.carId(), this.year()).subscribe({
      next: () => {
        this.busy.set(false);
        this.store.refresh();
        this.message.set({ type: 'ok', key: 'calculations.participants.deletedGoToRun' });
      },
      error: () => {
        this.busy.set(false);
        this.message.set({ type: 'error', key: 'calculations.messages.actionFailed' });
      },
    });
  }

  canDeactivate(): boolean {
    if (!this.dirty()) return true;
    return window.confirm(this.translate.instant('calculations.participants.unsavedConfirm'));
  }
}

function setsEqual(a: Set<number>, b: Set<number>): boolean {
  if (a.size !== b.size) return false;
  for (const id of a) if (!b.has(id)) return false;
  return true;
}

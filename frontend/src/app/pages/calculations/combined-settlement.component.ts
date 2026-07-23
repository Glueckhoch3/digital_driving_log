import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CalculationService } from '../../services/calculation.service';
import { CombinedSettlementRow } from '../../models/calculations';

const YEARS_BACK = 6;

@Component({
  selector: 'app-combined-settlement',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslateModule],
  templateUrl: './combined-settlement.component.html',
})
export class CombinedSettlementComponent implements OnInit {
  private readonly calculationService = inject(CalculationService);

  readonly years: number[] = [];
  year = 0;

  readonly rows = signal<CombinedSettlementRow[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  readonly totals = computed(() => {
    const rows = this.rows();
    return {
      paid: rows.reduce((sum, r) => sum + r.paid, 0),
      variable: rows.reduce((sum, r) => sum + r.differenceVariableCost, 0),
      fixed: rows.reduce((sum, r) => sum + r.differenceFixCost, 0),
      net: rows.reduce((sum, r) => sum + r.netBalance, 0),
    };
  });

  constructor() {
    const current = new Date().getFullYear();
    for (let y = current; y > current - YEARS_BACK; y--) {
      this.years.push(y);
    }
    this.year = current;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.calculationService.getCombined(this.year).subscribe({
      next: (rows) => {
        this.rows.set(rows);
        this.loading.set(false);
      },
      error: () => {
        this.rows.set([]);
        this.loading.set(false);
        this.error.set('calculations.messages.loadFailed');
      },
    });
  }
}

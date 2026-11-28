import { Component, computed, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { IconComponent } from '../../components/icon/icon.component';
import { ConsumptionService } from '../../services/consumption.service';
import { CarDto } from '../../models/cars';
import { extractApiErrorMessage } from '../../models/api-error';
import { selectableYears } from '../../utils/selectable-years';

/** Drawing area of the consumption line chart (SVG user units). */
const CHART = { width: 640, height: 260, left: 48, right: 16, top: 16, bottom: 40 } as const;

interface ChartPoint {
  x: number;
  y: number;
  label: string;
}

/** Fuel consumption per fill-up (l/100 km) for one car, optionally filtered by year. */
@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [RouterLink, TranslateModule, DecimalPipe, IconComponent],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.scss',
})
export class StatisticsComponent {
  private readonly consumptionService = inject(ConsumptionService);

  readonly car = input.required<CarDto>();

  readonly years = selectableYears();
  /** Selected year; `null` means all years. */
  readonly year = signal<number | null>(null);

  readonly chart = CHART;

  private readonly resource = rxResource({
    params: () => ({ carId: this.car().carId, year: this.year() }),
    stream: ({ params }) => this.consumptionService.getConsumption(params.carId, params.year),
  });

  readonly loading = this.resource.isLoading;
  readonly data = computed(() => this.resource.value() ?? null);
  readonly intervals = computed(() => this.data()?.intervals ?? []);

  readonly errorMessage = computed(() => {
    const err = this.resource.error();
    return err ? extractApiErrorMessage(err as HttpErrorResponse, 'statistics.loadFailed') : '';
  });

  /** Upper end of the y axis: the largest value rounded up to the next whole l/100 km. */
  readonly yMax = computed(() => {
    const max = Math.max(0, ...this.intervals().map((i) => i.litersPer100Km));
    return Math.max(1, Math.ceil(max));
  });

  readonly yTicks = computed(() => {
    const max = this.yMax();
    return [0, 0.25, 0.5, 0.75, 1].map((f) => ({
      value: Math.round(max * f * 10) / 10,
      y: this.toY(max * f),
    }));
  });

  readonly points = computed<ChartPoint[]>(() => {
    const intervals = this.intervals();
    const n = intervals.length;
    const plotWidth = CHART.width - CHART.left - CHART.right;
    return intervals.map((interval, index) => ({
      x: CHART.left + (n === 1 ? plotWidth / 2 : (plotWidth * index) / (n - 1)),
      y: this.toY(interval.litersPer100Km),
      label: interval.date,
    }));
  });

  readonly polyline = computed(() =>
    this.points()
      .map((p) => `${p.x},${p.y}`)
      .join(' '),
  );

  onYearChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.year.set(value === '' ? null : Number(value));
  }

  private toY(value: number): number {
    const plotHeight = CHART.height - CHART.top - CHART.bottom;
    return CHART.top + plotHeight - (plotHeight * value) / this.yMax();
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettlementService } from '../../services/settlement.service';
import { Settlement, SettlementReport } from '../../models/settlements';

@Component({
  selector: 'app-settlements',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './settlements.component.html',
  styleUrl: './settlements.component.scss'
})
export class SettlementsComponent implements OnInit {
  private settlementService = inject(SettlementService);

  settlements = signal<Settlement[]>([]);
  selectedSettlementReport = signal<SettlementReport | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    this.loadSettlements();
  }

  private loadSettlements(): void {
    this.isLoading.set(true);
    this.settlementService.getSettlements().subscribe({
      next: settlements => {
        this.settlements.set(settlements);
        this.isLoading.set(false);
      }
    });
  }

  triggerSettlement(): void {
    if (confirm('Are you sure you want to trigger a new settlement? This will finalize all current balances.')) {
      this.settlementService.triggerSettlement().subscribe({
        next: () => {
          this.loadSettlements();
        }
      });
    }
  }

  viewReport(settlementId: string | undefined): void {
    if (settlementId) {
      this.settlementService.getSettlementReport(settlementId).subscribe({
        next: report => {
          if (report) {
            this.selectedSettlementReport.set(report);
          }
        }
      });
    }
  }

  closeReport(): void {
    this.selectedSettlementReport.set(null);
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  getStatusBadgeClass(status: string): string {
    return status === 'completed' ? 'completed' : 'pending';
  }
}

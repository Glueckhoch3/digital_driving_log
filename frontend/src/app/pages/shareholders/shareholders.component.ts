import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShareholderService } from '../../services/shareholder.service';
import { Shareholder, ShareholderBalance } from '../../models/shareholders';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-shareholders',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './shareholders.component.html',
  styleUrl: './shareholders.component.scss'
})
export class ShareholdersComponent implements OnInit {
  private shareholderService = inject(ShareholderService);
  private fb = inject(FormBuilder);

  shareholders = signal<Shareholder[]>([]);
  balances = signal<ShareholderBalance[]>([]);
  shareholderForm!: FormGroup;
  isLoading = signal(true);
  isSubmitting = signal(false);
  successMessage = signal('');

  ngOnInit(): void {
    this.initializeForm();
    this.loadShareholders();
    this.loadBalances();
  }

  private initializeForm(): void {
    this.shareholderForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      participantType: ['permanent', Validators.required],
      startDate: [new Date().toISOString().split('T')[0], Validators.required],
      endDate: ['']
    });
  }

  private loadShareholders(): void {
    this.isLoading.set(true);
    this.shareholderService.getShareholders().subscribe({
      next: shareholders => {
        this.shareholders.set(shareholders);
        this.isLoading.set(false);
      }
    });
  }

  private loadBalances(): void {
    this.shareholderService.getShareholderBalances().subscribe({
      next: balances => {
        this.balances.set(balances);
      }
    });
  }

  onSubmit(): void {
    if (this.shareholderForm.valid) {
      this.isSubmitting.set(true);
      const formValue = this.shareholderForm.value;

      const request = {
        name: formValue.name,
        email: formValue.email,
        participantType: formValue.participantType,
        startDate: new Date(formValue.startDate),
        endDate: formValue.endDate ? new Date(formValue.endDate) : undefined
      };

      this.shareholderService.createShareholder(request).subscribe({
        next: () => {
          this.successMessage.set('Shareholder added successfully!');
          this.loadShareholders();
          this.shareholderForm.reset({
            name: '',
            email: '',
            participantType: 'permanent',
            startDate: new Date().toISOString().split('T')[0],
            endDate: ''
          });
          this.isSubmitting.set(false);

          setTimeout(() => this.successMessage.set(''), 3000);
        }
      });
    }
  }

  removeShareholder(id: string | undefined): void {
    if (id && confirm('Are you sure you want to remove this shareholder?')) {
      this.shareholderService.removeShareholder(id).subscribe({
        next: () => {
          this.loadShareholders();
          this.loadBalances();
        }
      });
    }
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}

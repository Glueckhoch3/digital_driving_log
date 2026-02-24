import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DrivesListComponent } from '../../components/drives-list/drives-list.component';

@Component({
  selector: 'app-driving-history',
  standalone: true,
  imports: [CommonModule, DrivesListComponent],
  templateUrl: './driving-history.component.html',
  styleUrl: './driving-history.component.scss'
})
export class DrivingHistoryComponent {}

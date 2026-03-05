import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DrivesListComponent } from '../../components/drives-list/drives-list.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-driving-history',
  standalone: true,
  imports: [CommonModule, DrivesListComponent, TranslateModule],
  templateUrl: './driving-history.component.html',
  styleUrl: './driving-history.component.scss'
})
export class DrivingHistoryComponent {}

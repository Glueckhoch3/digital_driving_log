import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DriveFormComponent } from '../../components/drive-form/drive-form.component';

@Component({
  selector: 'app-driving-log',
  standalone: true,
  imports: [CommonModule, DriveFormComponent],
  templateUrl: './driving-log.component.html',
  styleUrl: './driving-log.component.scss'
})
export class DrivingLogComponent {}

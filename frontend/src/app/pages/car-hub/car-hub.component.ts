import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { CarDto } from '../../models/cars';

@Component({
  selector: 'app-car-hub',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './car-hub.component.html',
  styleUrl: './car-hub.component.scss',
})
export class CarHubComponent {
  readonly car = input.required<CarDto>();
}

import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-calculation-hub',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './calculation-hub.component.html',
})
export class CalculationHubComponent {}

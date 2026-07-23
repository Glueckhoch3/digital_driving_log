import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { CarHubComponent } from './car-hub.component';
import { CarDto } from '../../models/cars';

describe('CarHubComponent', () => {
  let fixture: ComponentFixture<CarHubComponent>;

  const car: CarDto = { carId: 5, name: 'City Car', plateNumber: 'M-AB-1', ownerId: 1, data: null };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CarHubComponent],
      providers: [provideTranslateService(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(CarHubComponent);
    fixture.componentRef.setInput('car', car);
    fixture.detectChanges();
  });

  it('shows the car name and plate number', () => {
    expect(fixture.nativeElement.textContent).toContain('City Car');
    expect(fixture.nativeElement.textContent).toContain('M-AB-1');
  });

  it('renders links to all four child pages', () => {
    const hrefs = Array.from(fixture.nativeElement.querySelectorAll('a')).map((a) =>
      (a as HTMLAnchorElement).getAttribute('routerLink'),
    );
    expect(hrefs).toEqual(['drives', 'drives/new', 'costs', 'costs/new']);
  });
});

import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { ParticipantsPanelComponent } from './participants-panel.component';
import { AvailabilityStore } from './availability.store';
import { CalculationService } from '../../services/calculation.service';
import { CarAvailability, ParticipantSet } from '../../models/calculations';

describe('ParticipantsPanelComponent', () => {
  let fixture: ComponentFixture<ParticipantsPanelComponent>;
  let component: ParticipantsPanelComponent;
  let calculationService: Mocked<
    Pick<
      CalculationService,
      'getParticipants' | 'saveParticipants' | 'deleteParticipants' | 'getAvailability'
    >
  >;

  const participantSet: ParticipantSet = {
    carId: 5,
    year: 2025,
    stored: false,
    rows: [
      {
        userId: 1,
        userName: 'Anna',
        participating: true,
        manuallyAdded: false,
        hasDrives: true,
        distance: 100,
        fixShare: 33.33,
        varShare: 40,
      },
      {
        userId: 2,
        userName: 'Ben',
        participating: true,
        manuallyAdded: false,
        hasDrives: true,
        distance: 100,
        fixShare: 33.33,
        varShare: 40,
      },
      {
        userId: 3,
        userName: 'Carla',
        participating: true,
        manuallyAdded: false,
        hasDrives: true,
        distance: 50,
        fixShare: 33.34,
        varShare: 20,
      },
      {
        userId: 4,
        userName: 'Dan',
        participating: false,
        manuallyAdded: false,
        hasDrives: false,
        distance: 0,
        fixShare: null,
        varShare: null,
      },
    ],
  };

  function availability(yearCalculated: boolean): CarAvailability {
    return {
      carId: 5,
      years: [
        {
          year: 2025,
          yearCalculated,
          participantsStored: false,
          aggregatedMonths: [],
          monthsWithDrives: [],
        },
      ],
    };
  }

  async function setup(yearCalculated: boolean): Promise<void> {
    calculationService = {
      getParticipants: vi.fn().mockReturnValue(of(participantSet)),
      saveParticipants: vi.fn().mockReturnValue(of(undefined)),
      deleteParticipants: vi.fn().mockReturnValue(of(undefined)),
      getAvailability: vi.fn().mockReturnValue(of(availability(yearCalculated))),
    };

    await TestBed.configureTestingModule({
      imports: [ParticipantsPanelComponent],
      providers: [
        provideTranslateService(),
        { provide: CalculationService, useValue: calculationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ParticipantsPanelComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('carId', 5);
    fixture.componentRef.setInput('year', 2025);
    TestBed.inject(AvailabilityStore).load(5);
    fixture.detectChanges();
  }

  it('moves the previewed fixed share from 33.33 to 25.00 when a 4th participant is ticked', async () => {
    await setup(false);

    expect(component.preview().fix.get(1)).toBeCloseTo(33.33);

    component.toggle(participantSet.rows[3]);

    expect(component.preview().fix.get(1)).toBeCloseTo(25);
    expect(component.preview().fix.get(4)).toBeCloseTo(25);
  });

  it("disables a driver's checkbox so it can't be unticked", async () => {
    await setup(false);

    const checkboxes: HTMLInputElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('input[type="checkbox"]'),
    );
    // Anna (row 0) has drives and must be locked; Dan (row 3) has none and stays editable.
    expect(checkboxes[0].disabled).toBe(true);
    expect(checkboxes[3].disabled).toBe(false);
  });

  it('shows the stale-run hint after saving when the year is already calculated', async () => {
    await setup(true);

    component.save();

    expect(calculationService.saveParticipants).toHaveBeenCalled();
    expect(component.message()?.key).toBe('calculations.participants.savedStaleRun');
  });

  it('keeps unsaved ticks when reloaded after a calculation action', async () => {
    await setup(false);
    component.toggle(participantSet.rows[3]);

    component.reload();

    expect(component.checkedIds().has(4)).toBe(true);
    expect(component.dirty()).toBe(true);
  });

  it('drops unsaved ticks when the year changes', async () => {
    await setup(false);
    component.toggle(participantSet.rows[3]);

    fixture.componentRef.setInput('year', 2024);
    fixture.detectChanges();

    expect(component.dirty()).toBe(false);
  });

  it('is no longer dirty after saving', async () => {
    await setup(false);
    component.toggle(participantSet.rows[3]);
    calculationService.getParticipants.mockReturnValue(throwError(() => new Error('offline')));

    component.save();

    expect(component.dirty()).toBe(false);
  });
});

import { TestBed, ComponentFixture } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';
import { provideTranslateService } from '@ngx-translate/core';
import { FieldErrorComponent } from './field-error.component';

describe('FieldErrorComponent', () => {
  let fixture: ComponentFixture<FieldErrorComponent>;
  let component: FieldErrorComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FieldErrorComponent],
      providers: [provideTranslateService()],
    }).compileComponents();

    fixture = TestBed.createComponent(FieldErrorComponent);
    component = fixture.componentInstance;
  });

  it('renders nothing when the control has not been touched', () => {
    const control = new FormControl('', Validators.required);
    fixture.componentRef.setInput('control', control);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent.trim()).toBe('');
  });

  it('renders nothing when the control is valid', () => {
    const control = new FormControl('ok', Validators.required);
    control.markAsTouched();
    fixture.componentRef.setInput('control', control);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent.trim()).toBe('');
  });

  it('renders the required message once touched and empty', () => {
    const control = new FormControl('', Validators.required);
    control.markAsTouched();
    fixture.componentRef.setInput('control', control);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('validation.required');
  });

  it('renders the maxLength message with the configured limit', () => {
    const control = new FormControl('too long', Validators.maxLength(3));
    control.markAsTouched();
    fixture.componentRef.setInput('control', control);
    fixture.detectChanges();

    expect(component.entries()).toEqual([{ key: 'validation.maxLength', params: { max: 3 } }]);
  });

  it('renders the min message with the configured minimum', () => {
    const control = new FormControl(0, Validators.min(1));
    control.markAsTouched();
    fixture.componentRef.setInput('control', control);
    fixture.detectChanges();

    expect(component.entries()).toEqual([{ key: 'validation.min', params: { min: 1 } }]);
  });
});

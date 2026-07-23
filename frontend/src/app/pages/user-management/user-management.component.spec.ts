import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { UserManagementComponent } from './user-management.component';
import { UserService } from '../../services/user.service';
import { UserDto } from '../../models/users';

describe('UserManagementComponent', () => {
  let fixture: ComponentFixture<UserManagementComponent>;
  let component: UserManagementComponent;
  let userService: Mocked<
    Pick<UserService, 'getUsers' | 'createUser' | 'updateUser' | 'deleteUser'>
  >;

  const users: UserDto[] = [
    {
      userId: 1,
      firstname: 'Anna',
      lastname: 'Meyer',
      driverLicense: true,
      birthday: '1998-03-14',
    },
    { userId: 2, firstname: 'Ben', lastname: 'Schulz', driverLicense: false, birthday: null },
  ];

  beforeEach(async () => {
    userService = {
      getUsers: vi.fn().mockReturnValue(of(users)),
      createUser: vi.fn().mockReturnValue(of(users[0])),
      updateUser: vi.fn().mockReturnValue(of(users[0])),
      deleteUser: vi.fn().mockReturnValue(of(void 0)),
    };

    await TestBed.configureTestingModule({
      imports: [UserManagementComponent],
      providers: [provideTranslateService(), { provide: UserService, useValue: userService }],
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads and renders users on init', () => {
    expect(userService.getUsers).toHaveBeenCalled();
    expect(component.users()).toEqual(users);
    expect(component.loading()).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Anna');
  });

  it('shows load error when the list request fails', () => {
    userService.getUsers.mockReturnValue(throwError(() => ({ status: 500 })));

    component.loadUsers();

    expect(component.error()).toBe('userManagement.messages.loadFailed');
    expect(component.loading()).toBe(false);
  });

  it('does not submit an invalid form and shows a validation error', () => {
    component.userForm.setValue({
      firstname: '',
      lastname: '',
      driverLicense: false,
      birthday: '',
    });

    component.submit();
    fixture.detectChanges();

    expect(userService.createUser).not.toHaveBeenCalled();
    expect(component.userForm.touched).toBe(true);
    expect(component.error()).toBe('userManagement.messages.formInvalid');
    expect(fixture.nativeElement.textContent).toContain('userManagement.messages.formInvalid');
  });

  it('rejects a birthday that is today or in the future', () => {
    const today = new Date().toISOString().slice(0, 10);
    component.userForm.setValue({
      firstname: 'Max',
      lastname: 'Neu',
      driverLicense: false,
      birthday: today,
    });

    expect(component.userForm.controls.birthday.invalid).toBe(true);
    expect(component.userForm.controls.birthday.errors?.['pastDate']).toBe(true);
  });

  it('clears a stale success message when a new invalid submission is attempted', () => {
    component.message.set('userManagement.messages.createSuccess');
    component.userForm.setValue({
      firstname: '',
      lastname: '',
      driverLicense: false,
      birthday: '',
    });

    component.submit();
    fixture.detectChanges();

    expect(component.message()).toBe('');
    expect(fixture.nativeElement.textContent).not.toContain(
      'userManagement.messages.createSuccess',
    );
  });

  it('creates a user from a valid form and reloads the list', () => {
    component.userForm.setValue({
      firstname: 'Max',
      lastname: 'Neu',
      driverLicense: true,
      birthday: '1995-08-01',
    });

    component.submit();

    expect(userService.createUser).toHaveBeenCalledWith({
      firstname: 'Max',
      lastname: 'Neu',
      driverLicense: true,
      birthday: '1995-08-01',
    });
    expect(component.message()).toBe('userManagement.messages.createSuccess');
    expect(userService.getUsers).toHaveBeenCalledTimes(2);
  });

  it('updates the user being edited instead of creating', () => {
    component.edit(users[0]);
    component.userForm.patchValue({ lastname: 'Renamed' });

    component.submit();

    expect(userService.updateUser).toHaveBeenCalledWith(1, {
      firstname: 'Anna',
      lastname: 'Renamed',
      driverLicense: true,
      birthday: '1998-03-14',
    });
    expect(userService.createUser).not.toHaveBeenCalled();
    expect(component.editingUserId()).toBeNull();
  });

  it('shows create error when the create request fails', () => {
    userService.createUser.mockReturnValue(throwError(() => ({ status: 500 })));
    component.userForm.setValue({
      firstname: 'Max',
      lastname: 'Neu',
      driverLicense: false,
      birthday: '',
    });

    component.submit();

    expect(component.error()).toBe('userManagement.messages.createFailed');
  });

  it('deletes a user and reloads the list', () => {
    component.delete(2);

    expect(userService.deleteUser).toHaveBeenCalledWith(2);
    expect(component.message()).toBe('userManagement.messages.deleteSuccess');
    expect(userService.getUsers).toHaveBeenCalledTimes(2);
  });

  it('maps a 409 delete conflict to the dependency-blocked message', () => {
    userService.deleteUser.mockReturnValue(throwError(() => ({ status: 409 })));

    component.delete(1);

    expect(component.error()).toBe('userManagement.messages.deleteDependencyBlocked');
  });

  it('maps other delete errors to the generic delete message', () => {
    userService.deleteUser.mockReturnValue(throwError(() => ({ status: 500 })));

    component.delete(1);

    expect(component.error()).toBe('userManagement.messages.deleteFailed');
  });

  it('surfaces the backend detail message when the create request fails', () => {
    userService.createUser.mockReturnValue(
      throwError(() => ({
        status: 400,
        error: { detail: 'firstname must not exceed 63 characters' },
      })),
    );
    component.userForm.setValue({
      firstname: 'Max',
      lastname: 'Neu',
      driverLicense: false,
      birthday: '',
    });

    component.submit();

    expect(component.error()).toBe('firstname must not exceed 63 characters');
  });
});

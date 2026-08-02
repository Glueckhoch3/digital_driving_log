import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { UserService } from '../../services/user.service';
import { CreateUserRequest, UpdateUserRequest, UserDto } from '../../models/users';
import { extractApiErrorMessage } from '../../models/api-error';
import { pastDateValidator } from '../../validators/past-date.validator';
import { FieldErrorComponent } from '../../components/field-error/field-error.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, FieldErrorComponent],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss',
})
export class UserManagementComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly fb = inject(FormBuilder);

  readonly users = signal<UserDto[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly message = signal('');
  readonly editingUserId = signal<number | null>(null);

  readonly userForm = this.fb.group({
    firstname: ['', [Validators.required, Validators.maxLength(63)]],
    lastname: ['', [Validators.required, Validators.maxLength(63)]],
    driverLicense: [false],
    birthday: ['', pastDateValidator()],
  });

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'userManagement.messages.loadFailed'));
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    this.message.set('');
    this.error.set('');

    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      this.error.set('userManagement.messages.formInvalid');
      return;
    }

    const form = this.userForm.getRawValue();
    const payload: CreateUserRequest = {
      firstname: form.firstname ?? '',
      lastname: form.lastname ?? '',
      driverLicense: !!form.driverLicense,
      birthday: form.birthday || undefined,
    };

    const editingId = this.editingUserId();
    if (editingId == null) {
      this.userService.createUser(payload).subscribe({
        next: () => {
          this.message.set('userManagement.messages.createSuccess');
          this.error.set('');
          this.resetForm();
          this.loadUsers();
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(extractApiErrorMessage(err, 'userManagement.messages.createFailed'));
        },
      });
      return;
    }

    const updatePayload: UpdateUserRequest = payload;
    this.userService.updateUser(editingId, updatePayload).subscribe({
      next: () => {
        this.message.set('userManagement.messages.updateSuccess');
        this.error.set('');
        this.resetForm();
        this.loadUsers();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(extractApiErrorMessage(err, 'userManagement.messages.updateFailed'));
      },
    });
  }

  edit(user: UserDto): void {
    this.editingUserId.set(user.userId);
    this.userForm.patchValue({
      firstname: user.firstname,
      lastname: user.lastname,
      driverLicense: user.driverLicense,
      birthday: user.birthday ?? '',
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  delete(userId: number): void {
    this.message.set('');
    this.error.set('');
    this.userService.deleteUser(userId).subscribe({
      next: () => {
        this.message.set('userManagement.messages.deleteSuccess');
        this.error.set('');
        this.loadUsers();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err?.status === 409
            ? 'userManagement.messages.deleteDependencyBlocked'
            : extractApiErrorMessage(err, 'userManagement.messages.deleteFailed'),
        );
      },
    });
  }

  private resetForm(): void {
    this.editingUserId.set(null);
    this.userForm.reset({
      firstname: '',
      lastname: '',
      driverLicense: false,
      birthday: '',
    });
  }
}

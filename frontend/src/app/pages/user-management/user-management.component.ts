import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { UserService } from '../../services/user.service';
import { CreateUserRequest, UpdateUserRequest, UserDto } from '../../models/users';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule],
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
    birthday: [''],
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
      error: () => {
        this.error.set('userManagement.messages.loadFailed');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
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
        error: () => {
          this.error.set('userManagement.messages.createFailed');
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
      error: () => {
        this.error.set('userManagement.messages.updateFailed');
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
    this.userService.deleteUser(userId).subscribe({
      next: () => {
        this.message.set('userManagement.messages.deleteSuccess');
        this.error.set('');
        this.loadUsers();
      },
      error: (err) => {
        this.error.set(
          err?.status === 409
            ? 'userManagement.messages.deleteDependencyBlocked'
            : 'userManagement.messages.deleteFailed',
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

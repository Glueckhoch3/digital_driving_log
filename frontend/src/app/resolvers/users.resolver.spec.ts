import { TestBed } from '@angular/core/testing';
import { Observable, of } from 'rxjs';
import { vi, Mocked } from 'vitest';
import { usersResolver } from './users.resolver';
import { UserService } from '../services/user.service';
import { UserDto } from '../models/users';

describe('usersResolver', () => {
  it('resolves the full user list', () => {
    const users: UserDto[] = [
      { userId: 1, firstname: 'Anna', lastname: 'Meyer', driverLicense: true, birthday: null },
    ];
    const userService: Mocked<Pick<UserService, 'getUsers'>> = {
      getUsers: vi.fn().mockReturnValue(of(users)),
    };

    TestBed.configureTestingModule({
      providers: [{ provide: UserService, useValue: userService }],
    });

    const result = TestBed.runInInjectionContext(() => usersResolver({} as never, {} as never));

    let resolved: UserDto[] | undefined;
    (result as Observable<UserDto[]>).subscribe((value) => (resolved = value));

    expect(userService.getUsers).toHaveBeenCalled();
    expect(resolved).toEqual(users);
  });
});

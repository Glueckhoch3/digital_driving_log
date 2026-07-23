import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { UserService } from '../services/user.service';
import { UserDto } from '../models/users';

export const usersResolver: ResolveFn<UserDto[]> = () => inject(UserService).getUsers();

export interface UserDto {
  userId: number;
  firstname: string;
  lastname: string;
  driverLicense: boolean;
  birthday: string | null;
}

export interface CreateUserRequest {
  firstname: string;
  lastname: string;
  driverLicense: boolean;
  birthday?: string;
}

export interface UpdateUserRequest extends CreateUserRequest {}

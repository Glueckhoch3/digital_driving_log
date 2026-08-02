package de.digidrivelog.mappers;

import de.digidrivelog.dto.user.*;
import de.digidrivelog.models.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserDto toDto(User u) {
        if (u == null) return null;
        return new UserDto(
                u.getUserId(),
                u.getFirstname(),
                u.getLastname(),
                u.getDriverLicense(),
                u.getBirthday()
        );
    }

    public static User fromCreate(CreateUserRequest r) {
        if (r == null) return null;
        User u = new User();
        u.setFirstname(r.getFirstname());
        u.setLastname(r.getLastname());
        u.setDriverLicense(r.getDriverLicense());
        u.setBirthday(r.getBirthday());
        return u;
    }

    public static void applyUpdate(UpdateUserRequest r, User existing) {
        if (r == null || existing == null) return;
        if (r.getFirstname() != null) existing.setFirstname(r.getFirstname());
        if (r.getLastname() != null) existing.setLastname(r.getLastname());
        if (r.getDriverLicense() != null) existing.setDriverLicense(r.getDriverLicense());
        if (r.getBirthday() != null) existing.setBirthday(r.getBirthday());
    }
}

package de.digidrivelog.services;

import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UpdateUserRequest;
import de.digidrivelog.models.User;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    public UserDto createUser(CreateUserRequest request) {
        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDriverLicense(request.getDriverLicense());
        user.setBirthday(request.getBirthday());
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserMapper.toDto(user);
    }

    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDriverLicense(request.getDriverLicense());
        user.setBirthday(request.getBirthday());
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        try {
            userRepository.deleteById(userId);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User cannot be deleted because related cars, drives or costs exist");
        }
    }

    
}

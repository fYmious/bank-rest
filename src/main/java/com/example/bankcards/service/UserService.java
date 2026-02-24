package com.example.bankcards.service;

import com.example.bankcards.dto.UpdateUserRoleRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public UserResponse getUser(Long id) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found: " + id)
            );
        return toResponse(user);
    }

    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found: " + id)
            );
        user.setRole(request.getRole());
        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}

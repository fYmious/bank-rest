package com.example.bankcards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateUsername_throwsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setEmail("new@test.com");
        req.setPassword("password");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Username already taken");
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@test.com");
        req.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        User savedUser = User.builder()
            .id(1L)
            .username("newuser")
            .email("new@test.com")
            .password("encoded")
            .role(Role.ROLE_USER)
            .build();
        when(userRepository.save(any())).thenReturn(savedUser);

        UserDetails mockDetails =
            org.springframework.security.core.userdetails.User.withUsername(
                "newuser"
            )
                .password("encoded")
                .authorities("ROLE_USER")
                .build();
        when(userDetailsService.loadUserByUsername("newuser")).thenReturn(
            mockDetails
        );
        when(jwtService.generateToken(any())).thenReturn("token");

        var response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("token");
        assertThat(response.getUsername()).isEqualTo("newuser");
        verify(userRepository).save(any());
    }
}

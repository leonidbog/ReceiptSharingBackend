package com.example.demo.service;

import com.example.demo.controller.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRegistrationService = new UserRegistrationService(userRepository, passwordEncoder);
    }

    @Test
    void registerUserSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newUser");
        userDTO.setPassword("MySecret");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("MySecret")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L);
            return u;
        });

        assertDoesNotThrow(() -> userRegistrationService.registerUser(userDTO));

        verify(userRepository, times(1)).findByUsername("newUser");
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("MySecret");
    }

    @Test
    void registerUserUsernameTaken() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("existingUser");
        userDTO.setPassword("AnotherSecret");

        User existing = new User();
        existing.setId(5L);
        existing.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userRegistrationService.registerUser(userDTO));
        assertTrue(ex.getMessage().contains("already taken"));

        verify(userRepository, times(1)).findByUsername("existingUser");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerUserEmptyUsername() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("");
        userDTO.setPassword("password");

        // Допустим, если пустой username – кидаем IllegalArgumentException.
        // Это зависит от реализации, если в коде этого нет – нужно добавить.
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // Предполагаем, что сервис проверяет username на пустоту:
        // Если нет – нужно добавить такую проверку в сервис.

        // Например:
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userRegistrationService.registerUser(userDTO));
        assertTrue(ex.getMessage().contains("Username cannot be empty"));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerUserEmptyPassword() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("uniqueUser");
        userDTO.setPassword("");

        // Аналогично предыдущему тесту, если пустой пароль – ошибка
        when(userRepository.findByUsername("uniqueUser")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userRegistrationService.registerUser(userDTO));
        assertTrue(ex.getMessage().contains("Password cannot be empty"));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
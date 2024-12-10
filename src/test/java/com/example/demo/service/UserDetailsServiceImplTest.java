package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsernameSuccess() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var userDetails = userDetailsService.loadUserByUsername(username);
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsernameNotFound() {
        String username = "unknownUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(username));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsernameNullUsername() {
        // Проверяем поведение при null
        String username = null;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userDetailsService.loadUserByUsername(username));
        assertTrue(ex.getMessage().contains("Username cannot be null"));
        verify(userRepository, never()).findByUsername(anyString());
    }
}
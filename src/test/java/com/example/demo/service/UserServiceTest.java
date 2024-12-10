package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    void addFriendSuccess() {
        Long userId = 1L;
        Long friendId = 2L;

        User user = new User();
        user.setId(userId);
        user.setFriends(new HashSet<>());

        User friend = new User();
        friend.setId(friendId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.addFriend(userId, friendId);

        assertTrue(user.getFriends().contains(friend));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void addFriendUserNotFound() {
        Long userId = 1L;
        Long friendId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.addFriend(userId, friendId));
        assertTrue(ex.getMessage().contains("User not found"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addFriendFriendNotFound() {
        Long userId = 1L;
        Long friendId = 2L;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.addFriend(userId, friendId));
        assertTrue(ex.getMessage().contains("Friend not found"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeFriendSuccess() {
        Long userId = 1L;
        Long friendId = 2L;

        User user = new User();
        user.setId(userId);

        User friend = new User();
        friend.setId(friendId);

        user.setFriends(new HashSet<>(Set.of(friend)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.removeFriend(userId, friendId);

        assertFalse(user.getFriends().contains(friend));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void removeFriendUserNotFound() {
        Long userId = 1L;
        Long friendId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.removeFriend(userId, friendId));
        assertTrue(ex.getMessage().contains("User not found"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeFriendFriendNotFound() {
        Long userId = 1L;
        Long friendId = 2L;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.removeFriend(userId, friendId));
        assertTrue(ex.getMessage().contains("Friend not found"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getFriendsSuccess() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        User friend1 = new User();
        friend1.setId(2L);

        User friend2 = new User();
        friend2.setId(3L);

        user.setFriends(new HashSet<>(Set.of(friend1, friend2)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Set<User> friends = userService.getFriends(userId);
        assertEquals(2, friends.size());
        assertTrue(friends.contains(friend1));
        assertTrue(friends.contains(friend2));
    }

    @Test
    void getFriendsUserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getFriends(userId));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void getUserIdByUsernameSuccess() {
        String username = "testUser";
        User user = new User();
        user.setId(5L);
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Long resultId = userService.getUserIdByUsername(username);
        assertEquals(5L, resultId);
    }

    @Test
    void getUserIdByUsernameNotFound() {
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getUserIdByUsername(username));
        assertTrue(ex.getMessage().contains("User not found"));
    }
}
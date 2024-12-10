package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint для добавления друга
    @PostMapping("/friends/add/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable Long friendId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        userService.addFriend(userId, friendId);
        return ResponseEntity.ok("Friend added successfully");
    }

    @GetMapping("profile")
    public ResponseEntity<Long> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userId);
    }

    // Endpoint для удаления друга
    @PostMapping("/friends/remove/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendId, @AuthenticationPrincipal Principal principal) {
        Long userId = userService.getUserIdByUsername(principal.getName());
        userService.removeFriend(userId, friendId);
        return ResponseEntity.ok("Friend removed successfully");
    }

    // Endpoint для получения списка друзей
    @GetMapping("/friends")
    public ResponseEntity<?> getFriends(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        Set<User> friends = userService.getFriends(userId);

        // Преобразуем в DTO, чтобы не передавать лишние данные
        Set<UserDTO> friendDTOs = friends.stream()
                .map(friend -> new UserDTO(friend.getId(), friend.getUsername()))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(friendDTOs);
    }
}

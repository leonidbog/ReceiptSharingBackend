package com.example.demo.controller;

import com.example.demo.controller.dto.CreateGroupDTO;
import com.example.demo.dto.GroupDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@AuthenticationPrincipal User currentUser,
                                         @RequestBody CreateGroupDTO createGroupDTO) {
        try {
            Group group = groupService.createGroup(currentUser, createGroupDTO.getName(), createGroupDTO.getFriendIds());
            GroupDTO groupDTO = convertToDTO(group);
            return ResponseEntity.ok(groupDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Метод для преобразования Group в GroupDTO
    private GroupDTO convertToDTO(Group group) {
        Set<UserDTO> memberDTOs = group.getMembers().stream()
                .map(member -> new UserDTO(member.getId(), member.getUsername()))
                .collect(Collectors.toSet());
        return new GroupDTO(group.getId(), group.getName(), memberDTOs);
    }

    // Добавление участника в группу
    @PostMapping("/{groupId}/add_member/{userId}")
    public ResponseEntity<?> addMember(@AuthenticationPrincipal User currentUser,
                                       @PathVariable Long groupId,
                                       @PathVariable Long userId) {
        try {
            String message = groupService.addMember(currentUser, groupId, userId);
            return ResponseEntity.ok(message);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    // Удаление участника из группы
    @PostMapping("/{groupId}/remove_member/{userId}")
    public ResponseEntity<?> removeMember(@AuthenticationPrincipal User currentUser,
                                          @PathVariable Long groupId,
                                          @PathVariable Long userId) {
        try {
            String message = groupService.removeMember(currentUser, groupId, userId);
            return ResponseEntity.ok(message);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    // Получение списка групп пользователя
    @GetMapping("/my_groups")
    public ResponseEntity<?> getMyGroups(@AuthenticationPrincipal User currentUser) {
        List<GroupDTO> groups = groupService.getMyGroupsDTO(currentUser);
        return ResponseEntity.ok(groups);
    }
}
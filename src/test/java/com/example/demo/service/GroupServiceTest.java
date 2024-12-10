package com.example.demo.service;

import com.example.demo.dto.GroupDTO;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        groupService = new GroupService(groupRepository, userRepository);
    }

    @Test
    void createGroupSuccess() {
        User creator = new User();
        creator.setId(1L);

        String groupName = "New Group";
        Group savedGroup = new Group();
        savedGroup.setId(10L);
        savedGroup.setName(groupName);

        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(groupRepository.save(any())).thenAnswer(invocation -> {
            Group g = invocation.getArgument(0);
            g.setId(10L);
            return g;
        });

        Group result = groupService.createGroup(creator, groupName, Set.of(creator.getId()));

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(groupName, result.getName());
        assertTrue(result.getMembers().contains(creator));
        // Проверка что save был вызван
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void createGroupUserNotFound() {
        User creator = new User();
        creator.setId(2L);
        String groupName = "Group Without User";

        when(userRepository.findById(creator.getId())).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> groupService.createGroup(creator, groupName, Set.of(creator.getId())));
        assertTrue(ex.getMessage().contains("user not found"));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void addMemberToGroupSuccess() {
        Long adminId = 1L;
        User admin = new User();
        admin.setId(adminId);

        Long groupId = 10L;
        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(admin)));
        group.setCreator(admin);

        Long newUserId = 2L;
        User newUser = new User();
        newUser.setId(newUserId);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String updatedGroup = groupService.addMember(admin, groupId, newUserId);

        assertTrue(updatedGroup.contains("success"));
        verify(groupRepository, times(1)).save(group);
    }

    @Test
    void addMemberNotAnAdmin() {
        Long adminId = 1L;
        User admin = new User();
        admin.setId(adminId);

        Long groupId = 10L;
        User anotherUser = new User();
        anotherUser.setId(3L);

        User anotherUser2 = new User();
        anotherUser.setId(5L);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(admin, anotherUser, anotherUser2)));
        // Допустим creator - другой пользователь, не admin:
        User creator = new User();
        creator.setId(4L);
        group.setCreator(creator);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(5L)).thenReturn(Optional.of(anotherUser2));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> groupService.addMember(admin, groupId, 5L));
        assertTrue(ex.getMessage().contains("Not an admin"));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void removeMemberFromGroupSuccess() {
        Long adminId = 1L;
        User admin = new User();
        admin.setId(adminId);

        Long userToRemoveId = 2L;
        User userToRemove = new User();
        userToRemove.setId(userToRemoveId);

        Long groupId = 20L;
        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(admin, userToRemove)));
        group.setCreator(admin);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(userToRemoveId)).thenReturn(Optional.of(userToRemove));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String updated = groupService.removeMember(admin, groupId, userToRemoveId);
        assertTrue(updated.contains("success"));
        verify(groupRepository, times(1)).save(group);
    }

    @Test
    void getGroupByIdSuccess() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Long groupId = 30L;
        Group group = new Group();
        group.setId(groupId);
        group.setMembers(Set.of(currentUser));

        when(userRepository.existsById(currentUserId)).thenReturn(true);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.findAllByCreatorId(currentUserId)).thenReturn(Set.of(group));

        List<GroupDTO> result = groupService.getMyGroupsDTO(currentUser);
        assertEquals(1, result.size());
        assertNotNull(result);
        assertEquals(groupId, result.get(0).getId());
    }


    @Test
    void getGroupUserNotFound() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Long groupId = 50L;

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> groupService.getMyGroupsDTO(currentUser));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
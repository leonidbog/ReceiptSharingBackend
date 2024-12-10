package com.example.demo.service;

import com.example.demo.dto.GroupDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

//    // Создание новой группы
//    public Group createGroup(User currentUser, String name) {
//        Group group = new Group(name, currentUser);
//        return groupRepository.save(group);
//    }

    public Group createGroup(User currentUser, String name, Set<Long> friendIds) {
        if (userRepository.findById(currentUser.getId()).isEmpty()) {
            throw new NoSuchElementException("This user not found");
        }

        Group group = new Group(name, currentUser);
        User user = userRepository.findById(currentUser.getId()).get();
        group.addMember(user); // Создатель становится участником

        if (friendIds != null && !friendIds.isEmpty()) {
            Set<User> friendsToAdd = userRepository.findAllById(friendIds).stream()
                    .filter(friend -> user.getFriends().contains(friend))
                    .collect(Collectors.toSet());

            group.getMembers().addAll(friendsToAdd);
        }

        return groupRepository.save(group);
    }

    // Добавление участника в группу
    public String addMember(User currentUser, Long groupId, Long userId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Группа или пользователь не найдены");
        }

        Group group = groupOpt.get();
        User user = userOpt.get();

        // Проверяем, является ли текущий пользователь создателем группы
        if (!group.getCreator().getId().equals(currentUser.getId())) {
            throw new SecurityException("Not an admin");
        }

        group.addMember(user);
        groupRepository.save(group);

        return "Member successfully added";
    }

    // Удаление участника из группы
    public String removeMember(User currentUser, Long groupId, Long userId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Группа или пользователь не найдены");
        }

        Group group = groupOpt.get();
        User user = userOpt.get();

        if (!group.getCreator().getId().equals(currentUser.getId())) {
            throw new SecurityException("Только создатель группы может удалять участников");
        }

        if (group.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Нельзя удалить создателя группы");
        }

        group.removeMember(user);
        groupRepository.save(group);

        return "The user successfully removed";
    }

//    public List<GroupDTO> getMyGroupsDTO(User currentUser) {
//        if (!userRepository.existsById(currentUser.getId())) {
//            throw new IllegalArgumentException("This user not found");
//        }
//
//        // Инициализируем коллекцию
//        Set<Group> groups = groupRepository.findAllByCreatorId(currentUser.getId());
//
//        List<GroupDTO> groupDTOs = new ArrayList<>();
//        for (Group group : groups) {
//            // Преобразуем участников в UserDTO
//            Set<UserDTO> memberDTOs = group.getMembers().stream()
//                    .map(member -> new UserDTO(member.getId(), member.getUsername()))
//                    .collect(Collectors.toSet());
//
//            GroupDTO dto = new GroupDTO(group.getId(), group.getName(), memberDTOs);
//            groupDTOs.add(dto);
//        }
//        return groupDTOs;
//    }
    public List<GroupDTO> getMyGroupsDTO(User currentUser) {
        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("This user not found");
        }
        User user = userOpt.get();

        // Теперь берем все группы, в которых пользователь участвует
        Set<Group> groups = user.getGroups();

        List<GroupDTO> groupDTOs = new ArrayList<>();
        for (Group group : groups) {
            // Преобразуем участников в UserDTO
            Set<UserDTO> memberDTOs = group.getMembers().stream()
                    .map(member -> new UserDTO(member.getId(), member.getUsername()))
                    .collect(Collectors.toSet());

            GroupDTO dto = new GroupDTO(group.getId(), group.getName(), memberDTOs);
            groupDTOs.add(dto);
        }
        return groupDTOs;
    }

}
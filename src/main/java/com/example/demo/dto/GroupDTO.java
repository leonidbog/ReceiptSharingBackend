package com.example.demo.dto;

import java.util.Set;

public class GroupDTO {
    private Long id;
    private String name;
    private Set<UserDTO> members;

    // Конструктор
    public GroupDTO(Long id, String name, Set<UserDTO> members) {
        this.id = id;
        this.name = name;
        this.members = members;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<UserDTO> getMembers() {
        return members;
    }
}
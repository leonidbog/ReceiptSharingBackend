package com.example.demo.controller.dto;

import java.util.Set;

public class CreateGroupDTO {
    private String name;
    private Set<Long> friendIds; // Список ID друзей для добавления в группу

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public Set<Long> getFriendIds() {
        return friendIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFriendIds(Set<Long> friendIds) {
        this.friendIds = friendIds;
    }
}
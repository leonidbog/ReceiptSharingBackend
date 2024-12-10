package com.example.demo.dto;

public class UserDTO {
    private Long id;
    private String username;

    // Конструктор
    public UserDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
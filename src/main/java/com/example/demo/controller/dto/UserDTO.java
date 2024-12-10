package com.example.demo.controller.dto;

public class UserDTO {

    private Long id;
    private String username;
    private String password;  // Добавляем поле для пароля

    // Конструкторы
    public UserDTO() {
    }

    public UserDTO(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

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

    public String getPassword() {
        return password;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Создатель группы
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    // Участники группы
    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // Расходы по группе
    @OneToMany(mappedBy = "group")
    @JsonIgnore
    private Set<Expense> expenses = new HashSet<>();

    // Конструкторы
    public Group() {
    }

    public Group(String name, User creator) {
        this.name = name;
        this.creator = creator;
        this.members.add(creator); // Создатель автоматически становится участником
    }

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getCreator() {
        return creator;
    }

    public Set<User> getMembers() {
        return members;
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public void addMember(User user) {
        this.members.add(user);
        user.getGroups().add(this);
    }

    public void removeMember(User user) {
        this.members.remove(user);
        user.getGroups().remove(this);
    }

}
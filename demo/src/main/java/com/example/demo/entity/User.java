package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    private List<Repository> ownedRepositories;

    public User() {
    }

    public User(Long id, String username, String email, LocalDateTime createdAt, List<Repository> ownedRepositories) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.ownedRepositories = ownedRepositories;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Repository> getOwnedRepositories() {
        return ownedRepositories;
    }

    public void setOwnedRepositories(List<Repository> ownedRepositories) {
        this.ownedRepositories = ownedRepositories;
    }
}
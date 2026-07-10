package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "collaborators", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"repository_id", "user_id"})
})
public class Collaborator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    @JsonIgnoreProperties({"files", "comments", "collaborators", "hibernateLazyInitializer", "handler"})
    private Repository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"ownedRepositories", "hibernateLazyInitializer", "handler"})
    private User user;

    @Column(name = "can_write")
    private Boolean canWrite = false;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();

    public Collaborator() {
    }

    public Collaborator(Long id, Repository repository, User user, Boolean canWrite, LocalDateTime addedAt) {
        this.id = id;
        this.repository = repository;
        this.user = user;
        this.canWrite = canWrite;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getCanWrite() {
        return canWrite;
    }

    public void setCanWrite(Boolean canWrite) {
        this.canWrite = canWrite;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
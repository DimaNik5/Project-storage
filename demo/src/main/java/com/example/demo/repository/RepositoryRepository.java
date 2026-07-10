package com.example.demo.repository;

import com.example.demo.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {
    List<Repository> findByIsPublicTrue();
    List<Repository> findByOwnerId(Long ownerId);
    List<Repository> findByIsPublicTrueAndNameContainingIgnoreCase(String name);
}
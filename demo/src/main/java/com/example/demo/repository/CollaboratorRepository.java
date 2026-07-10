package com.example.demo.repository;

import com.example.demo.entity.Collaborator;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    List<Collaborator> findByRepositoryId(Long repositoryId);
    List<Collaborator> findByUserId(Long userId);
    Optional<Collaborator> findByRepositoryIdAndUserId(Long repositoryId, Long userId);
    boolean existsByRepositoryIdAndUserId(Long repositoryId, Long userId);
}

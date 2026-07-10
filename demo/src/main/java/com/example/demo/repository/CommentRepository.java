package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByRepositoryId(Long repositoryId);

    void deleteByRepositoryId(Long repositoryId);
}
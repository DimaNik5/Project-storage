package com.example.demo.repository;

import com.example.demo.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByRepositoryId(Long repositoryId);
    List<FileEntity> findByRepositoryIdAndCategory(Long repositoryId, String category);
    Optional<FileEntity> findByStoredFileName(String storedFileName);
    List<FileEntity> findByUploadedById(Long userId);
}
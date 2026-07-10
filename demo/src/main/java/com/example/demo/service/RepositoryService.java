package com.example.demo.service;

import com.example.demo.dto.RepositoryDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepositoryService {

    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    public Repository createRepository(String name, String description, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Repository repo = new Repository();
        repo.setName(name);
        repo.setDescription(description);
        repo.setOwner(owner);
        repo.setIsPublic(true);

        return repositoryRepository.save(repo);
    }

    public List<RepositoryDTO> getPublicRepositories() {
        return repositoryRepository.findByIsPublicTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RepositoryDTO getRepository(Long repoId) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        return convertToDTO(repo);
    }

    public List<RepositoryDTO> getUserRepositories(Long userId) {
        return repositoryRepository.findByOwnerId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void addCollaborator(Long repoId, Long userId, boolean canWrite) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!collaboratorRepository.existsByRepositoryIdAndUserId(repoId, userId)) {
            Collaborator collaborator = new Collaborator();
            collaborator.setRepository(repo);
            collaborator.setUser(user);
            collaborator.setCanWrite(canWrite);
            collaboratorRepository.save(collaborator);
        }
    }

    public boolean canUserWrite(Long repoId, Long userId) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        if (repo.getOwner().getId().equals(userId)) {
            return true;
        }

        return collaboratorRepository.findByRepositoryIdAndUserId(repoId, userId)
                .map(Collaborator::getCanWrite)
                .orElse(false);
    }

    public boolean canUserAccess(Long repoId, Long userId) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        if (repo.getIsPublic()) {
            return true;
        }

        return repo.getOwner().getId().equals(userId) ||
                collaboratorRepository.existsByRepositoryIdAndUserId(repoId, userId);
    }

    private RepositoryDTO convertToDTO(Repository repo) {
        RepositoryDTO dto = new RepositoryDTO();
        dto.setId(repo.getId());
        dto.setName(repo.getName());
        dto.setDescription(repo.getDescription());
        dto.setOwnerName(repo.getOwner().getUsername());
        dto.setOwnerId(repo.getOwner().getId());
        dto.setIsPublic(repo.getIsPublic());
        dto.setFileCount(repo.getFiles() != null ? repo.getFiles().size() : 0);
        dto.setCollaboratorCount(repo.getCollaborators() != null ? repo.getCollaborators().size() : 0);
        dto.setCreatedAt(repo.getCreatedAt());
        dto.setUpdatedAt(repo.getUpdatedAt());
        return dto;
    }
}
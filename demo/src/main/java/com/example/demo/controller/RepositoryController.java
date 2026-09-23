package com.example.demo.controller;

import com.example.demo.dto.RepositoryDTO;
import com.example.demo.entity.Repository;
import com.example.demo.entity.User;
import com.example.demo.repository.RepositoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.CollaboratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @GetMapping("/public")
    public ResponseEntity<List<Map<String, Object>>> getPublicRepositories() {
        List<Repository> repos = repositoryRepository.findByIsPublicTrue();
        List<Map<String, Object>> result = repos.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserRepositories(@PathVariable Long userId) {
        List<Repository> repos = repositoryRepository.findByOwnerId(userId);
        List<Map<String, Object>> result = repos.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("{repoId}")
    public ResponseEntity<?> getRepository(@PathVariable Long repoId) {
        Optional<Repository> repo = repositoryRepository.findById(repoId);
        if (repo.isPresent()) {
            return ResponseEntity.ok(convertToMap(repo.get()));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Repository not found"));
    }

    @PostMapping
    public ResponseEntity<?> createRepository(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Long userId) {
        try {
            User owner = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Repository repo = new Repository();
            repo.setName(name);
            repo.setDescription(description);
            repo.setOwner(owner);
            repo.setIsPublic(true);
            repo = repositoryRepository.save(repo);

            return ResponseEntity.ok(convertToMap(repo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{repoId}/collaborators")
    public ResponseEntity<?> addCollaborator(
            @PathVariable Long repoId,
            @RequestParam Long userId,
            @RequestParam boolean canWrite,
            @RequestParam Long requestedBy) {
        try {
            Repository repo = repositoryRepository.findById(repoId)
                    .orElseThrow(() -> new RuntimeException("Repository not found"));

            if (!repo.getOwner().getId().equals(requestedBy)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Only owner can add collaborators"));
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!collaboratorRepository.existsByRepositoryIdAndUserId(repoId, userId)) {
                com.example.demo.entity.Collaborator collaborator =
                        new com.example.demo.entity.Collaborator();
                collaborator.setRepository(repo);
                collaborator.setUser(user);
                collaborator.setCanWrite(canWrite);
                collaboratorRepository.save(collaborator);
            }

            return ResponseEntity.ok(Map.of("message", "Collaborator added"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{repoId}/permissions/{userId}")
    public ResponseEntity<?> checkPermissions(
            @PathVariable Long repoId,
            @PathVariable Long userId) {
        try {
            Repository repo = repositoryRepository.findById(repoId)
                    .orElseThrow(() -> new RuntimeException("Repository not found"));

            boolean canWrite = repo.getOwner().getId().equals(userId) ||
                    collaboratorRepository.findByRepositoryIdAndUserId(repoId, userId)
                            .map(c -> c.getCanWrite())
                            .orElse(false);

            boolean canAccess = repo.getIsPublic() ||
                    repo.getOwner().getId().equals(userId) ||
                    collaboratorRepository.existsByRepositoryIdAndUserId(repoId, userId);

            return ResponseEntity.ok(Map.of(
                    "canWrite", canWrite,
                    "canAccess", canAccess
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(Repository repo) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", repo.getId());
        map.put("name", repo.getName());
        map.put("description", repo.getDescription());
        map.put("isPublic", repo.getIsPublic());
        map.put("createdAt", repo.getCreatedAt());
        map.put("updatedAt", repo.getUpdatedAt());
        map.put("fileCount", repo.getFileCount());
        map.put("collaboratorCount", repo.getCollaboratorCount());

        Map<String, Object> owner = new HashMap<>();
        owner.put("id", repo.getOwner().getId());
        owner.put("username", repo.getOwner().getUsername());
        map.put("owner", owner);
        map.put("ownerName", repo.getOwner().getUsername());
        map.put("ownerId", repo.getOwner().getId());

        return map;
    }
}
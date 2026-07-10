package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Repository;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.RepositoryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/repository/{repoId}")
    public ResponseEntity<?> getRepositoryComments(@PathVariable Long repoId) {
        try {
            if (!repositoryRepository.existsById(repoId)) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Repository not found"));
            }

            List<Comment> comments = commentRepository.findByRepositoryIdOrderByCreatedAtDesc(repoId);
            List<Map<String, Object>> result = comments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getComment(@PathVariable Long commentId) {
        try {
            Optional<Comment> comment = commentRepository.findById(commentId);
            if (comment.isPresent()) {
                return ResponseEntity.ok(convertToMap(comment.get()));
            }
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Comment not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/repository/{repoId}")
    public ResponseEntity<?> addComment(
            @PathVariable Long repoId,
            @RequestParam Long userId,
            @RequestParam String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Comment content cannot be empty"));
            }

            if (content.length() > 1000) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Comment is too long (max 1000 characters)"));
            }

            Repository repository = repositoryRepository.findById(repoId)
                    .orElseThrow(() -> new RuntimeException("Repository not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Comment comment = new Comment();
            comment.setRepository(repository);
            comment.setUser(user);
            comment.setContent(content.trim());
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());

            comment = commentRepository.save(comment);

            return ResponseEntity.ok(convertToMap(comment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestParam String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Comment content cannot be empty"));
            }

            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            if (!comment.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You can only edit your own comments"));
            }

            comment.setContent(content.trim());
            comment.setUpdatedAt(LocalDateTime.now());
            comment = commentRepository.save(comment);

            return ResponseEntity.ok(convertToMap(comment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            boolean isAuthor = comment.getUser().getId().equals(userId);
            boolean isRepoOwner = comment.getRepository().getOwner().getId().equals(userId);

            if (!isAuthor && !isRepoOwner) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You don't have permission to delete this comment"));
            }

            commentRepository.delete(comment);

            return ResponseEntity.ok(Map.of(
                    "message", "Comment deleted successfully",
                    "commentId", commentId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserComments(@PathVariable Long userId) {
        try {
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "User not found"));
            }

            List<Comment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
            List<Map<String, Object>> result = comments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/count/{repoId}")
    public ResponseEntity<?> getCommentCount(@PathVariable Long repoId) {
        try {
            long count = commentRepository.countByRepositoryId(repoId);
            return ResponseEntity.ok(Map.of(
                    "repositoryId", repoId,
                    "commentCount", count
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(Comment comment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", comment.getId());
        map.put("content", comment.getContent());
        map.put("createdAt", comment.getCreatedAt());
        map.put("updatedAt", comment.getUpdatedAt());

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", comment.getUser().getId());
        userMap.put("username", comment.getUser().getUsername());
        map.put("user", userMap);
        map.put("username", comment.getUser().getUsername());
        map.put("userId", comment.getUser().getId());

        map.put("repositoryId", comment.getRepository().getId());
        map.put("repositoryName", comment.getRepository().getName());

        return map;
    }
}
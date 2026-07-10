package com.example.demo.controller;

import com.example.demo.entity.FileEntity;
import com.example.demo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload/{repoId}")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long repoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId,
            @RequestParam(required = false) String commitMessage) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File is empty"));
            }

            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File size exceeds maximum limit (50MB)"));
            }

            FileEntity savedFile = fileStorageService.uploadFile(file, repoId, userId, commitMessage);
            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "fileId", savedFile.getId(),
                    "fileName", savedFile.getOriginalFileName()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not upload file: " + e.getMessage()));
        }
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<?> updateFile(
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId) {
        try {
            FileEntity updatedFile = fileStorageService.updateFile(fileId, file, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "File updated successfully",
                    "fileId", updatedFile.getId(),
                    "fileName", updatedFile.getOriginalFileName()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not update file: " + e.getMessage()));
        }
    }

    @GetMapping("/repository/{repoId}")
    public ResponseEntity<?> getRepositoryFiles(@PathVariable Long repoId) {
        try {
            List<Map<String, Object>> files = fileStorageService.getRepositoryFiles(repoId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileId);

            // Получаем оригинальное имя файла
            String originalFileName = fileStorageService.getRepositoryFiles(null)
                    .stream()
                    .filter(f -> f.get("id").equals(fileId))
                    .findFirst()
                    .map(f -> f.get("originalFileName").toString())
                    .orElse("file");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "File not found: " + e.getMessage()));
        }
    }

    @GetMapping("/download/repository/{repoId}")
    public ResponseEntity<?> downloadRepositoryAsZip(@PathVariable Long repoId) {
        try {
            byte[] zipData = fileStorageService.getRepositoryFilesAsZip(repoId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"repository-" + repoId + ".zip\"")
                    .body(zipData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not create ZIP archive: " + e.getMessage()));
        }
    }

    @GetMapping("/download/all")
    public ResponseEntity<?> downloadAllFiles() {
        try {
            byte[] zipData = fileStorageService.getAllFilesAsZip();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"all-files.zip\"")
                    .body(zipData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not create ZIP archive: " + e.getMessage()));
        }
    }

    @GetMapping("/download/category/{repoId}")
    public ResponseEntity<?> downloadFilesByCategory(
            @PathVariable Long repoId,
            @RequestParam String category) {
        try {
            byte[] zipData = fileStorageService.getFilesByCategoryAsZip(repoId, category);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + category + "-files.zip\"")
                    .body(zipData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not create ZIP archive: " + e.getMessage()));
        }
    }


    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            @RequestParam Long userId) {
        try {
            fileStorageService.deleteFile(fileId, userId);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
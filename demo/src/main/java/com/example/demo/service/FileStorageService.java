package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.*;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public FileEntity uploadFile(MultipartFile file, Long repoId, Long userId, String commitMessage) throws IOException {

        Repository repository = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = Paths.get(uploadDir).resolve(storedFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setOriginalFileName(originalFileName);
        fileEntity.setStoredFileName(storedFileName);
        fileEntity.setFileType(file.getContentType());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setRepository(repository);
        fileEntity.setUploadedBy(user);
        fileEntity.setCommitMessage(commitMessage);
        fileEntity.setUploadedAt(LocalDateTime.now());
        fileEntity.setUpdatedAt(LocalDateTime.now());

        return fileRepository.save(fileEntity);
    }

    public FileEntity updateFile(Long fileId, MultipartFile file, Long userId) throws IOException {
        FileEntity existingFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!existingFile.getUploadedBy().getId().equals(userId) &&
                !existingFile.getRepository().getOwner().getId().equals(userId)) {
            throw new RuntimeException("No permission to update this file");
        }

        deleteFileFromDisk(existingFile.getStoredFileName());

        String fileExtension = getFileExtension(existingFile.getOriginalFileName());
        String storedFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = Paths.get(uploadDir).resolve(storedFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        existingFile.setStoredFileName(storedFileName);
        existingFile.setFileType(file.getContentType());
        existingFile.setFileSize(file.getSize());
        existingFile.setUpdatedAt(LocalDateTime.now());

        return fileRepository.save(existingFile);
    }

    public Resource loadFileAsResource(Long fileId) throws MalformedURLException {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));

        Path filePath = Paths.get(uploadDir).resolve(fileEntity.getStoredFileName());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("File not found: " + fileEntity.getOriginalFileName());
        }
    }

    public List<Map<String, Object>> getRepositoryFiles(Long repoId) {
        List<FileEntity> files = fileRepository.findByRepositoryId(repoId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (FileEntity file : files) {
            result.add(convertToMap(file));
        }

        return result;
    }

    public byte[] getRepositoryFilesAsZip(Long repoId) throws IOException {
        List<FileEntity> files = fileRepository.findByRepositoryId(repoId);
        return createZipArchive(files);
    }

    public byte[] getAllFilesAsZip() throws IOException {
        List<FileEntity> allFiles = fileRepository.findAll();
        return createZipArchive(allFiles);
    }

    public byte[] getFilesByCategoryAsZip(Long repoId, String category) throws IOException {
        List<FileEntity> files = fileRepository.findByRepositoryIdAndCategory(repoId, category);
        return createZipArchive(files);
    }

    public void deleteFile(Long fileId, Long userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fileEntity.getUploadedBy().getId().equals(userId) &&
                !fileEntity.getRepository().getOwner().getId().equals(userId)) {
            throw new RuntimeException("No permission to delete this file");
        }

        deleteFileFromDisk(fileEntity.getStoredFileName());

        fileRepository.delete(fileEntity);
    }


    private byte[] createZipArchive(List<FileEntity> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (FileEntity fileEntity : files) {
                Path filePath = Paths.get(uploadDir).resolve(fileEntity.getStoredFileName());
                if (Files.exists(filePath)) {
                    ZipEntry zipEntry = new ZipEntry(fileEntity.getOriginalFileName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = Files.readAllBytes(filePath);
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                }
            }
        }
        return baos.toByteArray();
    }

    private void deleteFileFromDisk(String storedFileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(storedFileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting file from disk: " + storedFileName);
            e.printStackTrace();
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private Map<String, Object> convertToMap(FileEntity file) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", file.getId());
        map.put("originalFileName", file.getOriginalFileName());
        map.put("fileType", file.getFileType());
        map.put("fileSize", file.getFileSize());
        map.put("category", file.getCategory());
        map.put("commitMessage", file.getCommitMessage());
        map.put("uploadedAt", file.getUploadedAt());
        map.put("updatedAt", file.getUpdatedAt());

        Map<String, Object> uploader = new HashMap<>();
        uploader.put("id", file.getUploadedBy().getId());
        uploader.put("username", file.getUploadedBy().getUsername());
        map.put("uploadedBy", uploader);
        map.put("uploadedByName", file.getUploadedBy().getUsername());

        map.put("repositoryId", file.getRepository().getId());
        map.put("repositoryName", file.getRepository().getName());

        return map;
    }
}
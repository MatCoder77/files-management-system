package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.file.boundary.FilesSearchCriteria;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.StorageService;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.url.UrlProvider;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.UploadInfo;
import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final UrlProvider urlProvider;

    public FileService(FileRepository fileRepository, StorageService storageService, UrlProvider urlProvider) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.urlProvider = urlProvider;
    }

    public Page<File> searchFilesByCriteria(FilesSearchCriteria searchCriteria, Pageable pageable) {
        return fileRepository.searchFilesByCriteria(searchCriteria, pageable);
    }

    public List<File> getFilesByIds(Collection<Long> ids) {
        return fileRepository.findAllById(ids);
    }

    public List<UploadInfo> uploadFiles(List<MultipartFile> multipartFiles, String path) {
        Map<UploadInfo, java.io.File> filesByUploadInfo = getFilesByUploadInfo(multipartFiles, path);
        Map<URI, java.io.File> filesByUrl = getFilesByUrl(filesByUploadInfo);
        storageService.saveResources(filesByUrl);
        filesByUrl.values().forEach(java.io.File::delete);
        return new ArrayList<>(filesByUploadInfo.keySet());
    }

    private Map<UploadInfo, java.io.File> getFilesByUploadInfo(List<MultipartFile> multipartFiles, String path) {
        return multipartFiles.stream()
                .collect(Collectors.toMap(file -> getUploadInfo(file, path), this::convertMultiPartFileToFile));
    }

    private UploadInfo getUploadInfo(MultipartFile file, String path) {
        return new UploadInfo(file.getOriginalFilename(), urlProvider.getUrlForResource(file, path));
    }

    private java.io.File convertMultiPartFileToFile(MultipartFile multipartFile) {
        java.io.File file = new java.io.File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
            throw new IllegalArgumentAppException("Cannot upload file: ", e);
        }
        return file;
    }

    private Map<URI, java.io.File> getFilesByUrl(Map<UploadInfo, java.io.File> filesByUploadInfo) {
        return filesByUploadInfo.entrySet().stream()
                .collect(Collectors.toMap(fileByUploadInfo -> fileByUploadInfo.getKey().getUrl(), Map.Entry::getValue));
    }

}

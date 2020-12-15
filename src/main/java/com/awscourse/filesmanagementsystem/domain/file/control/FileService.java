package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState;
import com.awscourse.filesmanagementsystem.domain.file.boundary.FilesSearchCriteria;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.StorageService;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.url.UrlProvider;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.UploadInfo;
import com.awscourse.filesmanagementsystem.domain.label.control.LabelService;
import com.awscourse.filesmanagementsystem.infrastructure.exception.ExceptionUtils;
import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final UrlProvider urlProvider;
    private final LabelService labelService;

    public File getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.getObjectNotFoundException(File.class, id));
    }

    public List<File> getFilesByIds(Collection<Long> ids) {
        return fileRepository.findAllById(ids);
    }

    public Page<File> searchFilesByCriteria(FilesSearchCriteria searchCriteria, Pageable pageable) {
        return fileRepository.searchFilesByCriteria(searchCriteria, pageable);
    }

    public List<File> createFiles(Collection<File> files) {
        Map<File, Resource> resourcesByFile = getResourcesByFile(files);
        validateBeforeCreate(resourcesByFile);
        prepareBeforeCreate(resourcesByFile);
        return fileRepository.saveAll(files);
    }

    private Map<File, Resource> getResourcesByFile(Collection<File> files) {
        return files.stream()
                .collect(Collectors.toMap(Function.identity(), file -> storageService.getResource(file.getUrl())));
    }

    private void validateBeforeCreate(Map<File, Resource> resourcesByFile) {
        validateIfResourcesExist(resourcesByFile);
        validateIfFullPathsAreUnique(resourcesByFile.keySet());
    }

    private void validateIfResourcesExist(Map<File, Resource> resourcesByFile) {
        Map<URI, Resource> nonExistingResourcesByUrl = getNonExistingResourcesByUrl(resourcesByFile);
        if (!nonExistingResourcesByUrl.isEmpty()) {
            throw new IllegalArgumentAppException(MessageFormat.format("Resources for url {0} do not exist", nonExistingResourcesByUrl.keySet()));
        }
    }

    private Map<URI, Resource> getNonExistingResourcesByUrl(Map<File, Resource> resourcesByFile) {
        return resourcesByFile.entrySet().stream()
                .filter(resourceByUrl -> !resourceByUrl.getValue().exists())
                .collect(Collectors.toMap(fileByResource -> fileByResource.getKey().getUrl(), Map.Entry::getValue));
    }

    private void validateIfFullPathsAreUnique(Collection<File> files) {
        validateIfThereAreNoFullPathDuplicatesAmongSubmittedFiles(files);
        validateIfThereAreNoFullPathDuplicatesAmongExistingFiles(files);
    }

    private void validateIfThereAreNoFullPathDuplicatesAmongSubmittedFiles(Collection<File> files) {
        List<String> fullPaths = getFullPaths(files);
        if (fullPaths.size() != new HashSet<>(fullPaths).size()) {
            throw new IllegalArgumentAppException("Full paths among submitted files are not unique");
        }
    }

    private List<String> getFullPaths(Collection<File> files) {
        return files.stream()
                .map(File::getFullPath)
                .collect(Collectors.toList());
    }

    private void validateIfThereAreNoFullPathDuplicatesAmongExistingFiles(Collection<File> files) {
        List<File> foundDuplicates = fileRepository.findAllByFullPathInAndIdNotIn(getFullPaths(files), getUniqueIds(files));
        if (foundDuplicates.size() > 0) {
            throw new IllegalArgumentAppException(MessageFormat.format("There are already existing files with full path {0}", getFullPaths(foundDuplicates)));
        }
    }

    private List<Long> getUniqueIds(Collection<File> files) {
        return files.stream()
                .map(File::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void prepareBeforeCreate(Map<File, Resource> resourcesByFile) {
        resourcesByFile.forEach(this::prepareBeforeCreate);
    }

    private void prepareBeforeCreate(File file, Resource resource) {
        file.setId(null);
        file.setObjectState(ObjectState.ACTIVE);
        file.setSize(getSize(resource));
    }

    private long getSize(Resource resource) {
        try {
            return resource.contentLength();
        } catch (IOException exception) {
            throw new IllegalArgumentAppException("Cannot create file, error during acquiring resource size.");
        }
    }

    public Resource downloadResource(Long fileId) {
        File file = getFileById(fileId);
        return storageService.getResource(file.getUrl());
    }

    public List<Resource> downloadResources(Collection<Long> fileIds) {
        List<File> files = getFilesByIds(fileIds);
        return storageService.getResources(getUrls(files));
    }

    private List<URI> getUrls(Collection<File> files) {
        return files.stream()
                .map(File::getUrl)
                .collect(Collectors.toList());
    }

    public List<UploadInfo> uploadResources(Collection<Resource> resources, String path) {
        Map<UploadInfo, Resource> resourcesByUploadInfo = getResourceByUploadInfo(resources, path);
        Map<URI, Resource> resourcesByUrl = getResourcesByUrl(resourcesByUploadInfo);
        storageService.saveResources(resourcesByUrl);
        return new ArrayList<>(resourcesByUploadInfo.keySet());
    }

    private Map<UploadInfo, Resource> getResourceByUploadInfo(Collection<Resource> resources, String path) {
        return resources.stream()
                .collect(Collectors.toMap(file -> getUploadInfo(file, path), Function.identity()));
    }

    private UploadInfo getUploadInfo(Resource resource, String path) {
        return new UploadInfo(resource.getFilename(), urlProvider.getUrlForResource(resource, path));
    }

    private Map<URI, Resource> getResourcesByUrl(Map<UploadInfo, Resource> resourcesByUploadInfo) {
        return resourcesByUploadInfo.entrySet().stream()
                .collect(Collectors.toMap(fileByUploadInfo -> fileByUploadInfo.getKey().getUrl(), Map.Entry::getValue));
    }

}

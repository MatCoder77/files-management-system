package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState;
import com.awscourse.filesmanagementsystem.domain.file.boundary.FileBulkDeletedEvent;
import com.awscourse.filesmanagementsystem.domain.file.boundary.FilesSearchCriteria;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.StorageService;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.url.UrlProvider;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.FileResource;
import com.awscourse.filesmanagementsystem.domain.file.entity.UploadInfo;
import com.awscourse.filesmanagementsystem.infrastructure.exception.ExceptionUtils;
import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import com.awscourse.filesmanagementsystem.infrastructure.transform.TransformUtils;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final UrlProvider urlProvider;
    private final ApplicationEventPublisher eventPublisher;

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
        validateIfResourcesAreNotAlreadyAssociatedWithDifferentFile(resourcesByFile);
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

    private void validateIfResourcesAreNotAlreadyAssociatedWithDifferentFile(Map<File, Resource> resourcesByFile) {
        List<File> foundDuplicates = findDuplicatedUrlsAmongExistingFiles(resourcesByFile.keySet());
        if (!foundDuplicates.isEmpty()) {
            throw new IllegalArgumentAppException(MessageFormat.format("There are files already associated with supplied urls {0}", getNonNullUniqueIds(foundDuplicates)));
        }
    }

    private List<File> findDuplicatedUrlsAmongExistingFiles(Collection<File> files) {
        Set<Long> fileIds = getNonNullUniqueIds(files);
        if (fileIds.isEmpty()) {
            return fileRepository.findAllByUrlIn(getUrls(files));
        }
        return fileRepository.findAllByUrlInAndIdNotIn(getUrls(files), fileIds);
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
        List<File> foundDuplicates = findDuplicatedFullPathsAmongExistingFiles(files);
        if (!foundDuplicates.isEmpty()) {
            throw new IllegalArgumentAppException(MessageFormat.format("There are already existing files with full path {0}", getFullPaths(foundDuplicates)));
        }
    }

    private List<File> findDuplicatedFullPathsAmongExistingFiles(Collection<File> files) {
        Set<Long> fileIds = getNonNullUniqueIds(files);
        if (fileIds.isEmpty()) {
            return fileRepository.findAllByFullPathIn(getFullPaths(files));
        }
        return fileRepository.findAllByFullPathInAndIdNotIn(getFullPaths(files), fileIds);
    }

    private Set<Long> getNonNullUniqueIds(Collection<File> files) {
        return files.stream()
                .map(File::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
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

    public void updateFiles(Collection<File> updatedFiles, Long userId) {
        List<File> foundFiles = fileRepository.findAllById(getNonNullUniqueIds(updatedFiles));
        Map<File, Resource> resourcesByUpdatedFile = getResourcesByFile(updatedFiles);
        validateBeforeUpdate(foundFiles, resourcesByUpdatedFile, userId);
        prepareBeforeUpdate(resourcesByUpdatedFile);
        updateFiles(foundFiles, updatedFiles);
    }

    public void validateBeforeUpdate(Collection<File> existingFiles, Map<File, Resource> resourceByUpdatedFile, Long userId) {
        validateIfAllFilesHaveUniqueId(resourceByUpdatedFile.keySet());
        validateIfAllFilesExist(getNonNullUniqueIds(existingFiles), resourceByUpdatedFile.keySet());
        validateIfResourcesExist(resourceByUpdatedFile);
        validateIfFullPathsAreUnique(resourceByUpdatedFile.keySet());
    }

    private void validateIfAllFilesHaveUniqueId(Collection<File> files) {
        if (getNonNullUniqueIds(files).size() != files.size()) {
            throw new IllegalArgumentAppException("There are some files without id or ids are not unique!");
        }
    }

    public void validateIfAllFilesExist(Collection<Long> ids, Collection<File> foundFiles) {
        Set<Long> idsOfNonExistingFiles = getIdsOfNonExistingFiles(ids, foundFiles);
        if (!idsOfNonExistingFiles.isEmpty()) {
            throw ExceptionUtils.getObjectNotFoundException(File.class, idsOfNonExistingFiles);
        }
    }

    private Set<Long> getIdsOfNonExistingFiles(Collection<Long> ids, Collection<File> foundFiles) {
        return Sets.difference(new HashSet<>(ids), getNonNullUniqueIds(foundFiles));
    }

    private void prepareBeforeUpdate(Map<File, Resource> resourcesByFile) {
        resourcesByFile.forEach(this::prepareBeforeUpdate);
    }

    private void prepareBeforeUpdate(File file, Resource resource) {
        file.setSize(getSize(resource));
    }

    private void updateFiles(Collection<File> existingFiles, Collection<File> updatedFiles) {
        Map<Long, File> updatedFilesById = TransformUtils.transformToMap(updatedFiles, File::getId, Function.identity());
        existingFiles.forEach(existingFile -> updateFile(existingFile, updatedFilesById.get(existingFile.getId())));
    }

    private void updateFile(File existingFile, File updatedFile) {
        existingFile.setName(updatedFile.getName());
        existingFile.setPath(updatedFile.getPath());
        existingFile.setFullPath(updatedFile.getFullPath());
        existingFile.setDescription(updatedFile.getDescription());
        existingFile.setSize(updatedFile.getSize());
        existingFile.setUrl(updatedFile.getUrl());
    }

    public void deleteFiles(Collection<Long> ids) {
        List<File> foundFiles = fileRepository.findAllById(ids);
        validateBeforeDelete(ids, foundFiles);
        foundFiles.forEach(file -> file.setObjectState(ObjectState.REMOVED));
        publishFileBulkDeletedEvent(foundFiles);
    }

    private void validateBeforeDelete(Collection<Long> ids, Collection<File> files) {
        validateIfAllFilesExist(ids, files);
    }

    private void publishFileBulkDeletedEvent(List<File> deletedFiles) {
        eventPublisher.publishEvent(new FileBulkDeletedEvent(this, deletedFiles));
    }

    public FileResource downloadResource(Long fileId) {
        File file = getFileById(fileId);
        return new FileResource(file, storageService.getResource(file.getUrl()));
    }

    public List<FileResource> downloadResources(Collection<Long> fileIds) {
        List<File> files = getFilesByIds(fileIds);
        Map<URI, Resource> resourcesByUrl = storageService.getResources(getUrls(files));
        Map<URI, File> fileByUrl = TransformUtils.transformToMap(files, File::getUrl, Function.identity());
        return resourcesByUrl.entrySet().stream()
                .map(urlAndResource -> new FileResource(fileByUrl.get(urlAndResource.getKey()), urlAndResource.getValue()))
                .collect(Collectors.toList());
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

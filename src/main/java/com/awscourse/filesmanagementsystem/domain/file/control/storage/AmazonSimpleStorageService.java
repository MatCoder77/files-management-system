package com.awscourse.filesmanagementsystem.domain.file.control.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AmazonSimpleStorageService implements StorageService {

    private final ResourceLoader resourceLoader;
    private final ResourcePatternResolver resourcePatternResolver;

//    @Autowired
//    ResourceLoader resourceLoader;
//
//    private ResourcePatternResolver resourcePatternResolver;
//
//    @Autowired
//    public void setupResolver(ApplicationContext applicationContext, AmazonS3 amazonS3) {
//        this.resourcePatternResolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
//    }

    public AmazonSimpleStorageService(ResourceLoader resourceLoader, AmazonS3 amazonS3, ApplicationContext applicationContext) {
        this.resourceLoader = resourceLoader;
        this.resourcePatternResolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
    }

    public void downloadS3Object(String s3Url) throws IOException {
        Resource resource = resourceLoader.getResource(s3Url);
        File downloadedS3Object = new File(resource.getFilename());
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, downloadedS3Object.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void uploadFileToS3(File file, String s3Url) throws IOException {
        WritableResource resource = (WritableResource) resourceLoader.getResource(s3Url);
        try (OutputStream outputStream = resource.getOutputStream()) {
            Files.copy(file.toPath(), outputStream);
        }
    }

    public void downloadMultipleS3Objects(String s3UrlPattern) throws IOException {
        Resource[] allFileMatchingPatten = this.resourcePatternResolver.getResources(s3UrlPattern);
        for (Resource resource : allFileMatchingPatten) {
            String fileName = resource.getFilename();
            fileName = fileName.substring(0, fileName.lastIndexOf("/") + 1);
            File downloadedS3Object = new File(fileName);
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, downloadedS3Object.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Override
    public File getResource(String relativePath) {
        return null;
    }

    @Override
    public List<File> getResources(Collection<String> relativePaths) {
        return null;
    }

    @Override
    public String saveResource(File file, String relativePath) {
        return null;
    }

    @Override
    public String saveResources(Map<String, File> filesByRelativePath) {
        return null;
    }
}

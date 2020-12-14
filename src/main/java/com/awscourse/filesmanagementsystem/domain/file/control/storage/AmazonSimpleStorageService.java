package com.awscourse.filesmanagementsystem.domain.file.control.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.awscourse.filesmanagementsystem.domain.file.control.storage.url.AmazonS3UrlProvider;
import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import com.awscourse.filesmanagementsystem.infrastructure.exception.ThrowingConsumer;
import com.awscourse.filesmanagementsystem.infrastructure.exception.ThrowingRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component("AmazonSimpleStorageService")
public class AmazonSimpleStorageService implements StorageService {

    private final ResourceLoader resourceLoader;
    private final ResourcePatternResolver resourcePatternResolver;
    private final AsyncTaskExecutor executor;

//    @Autowired
//    ResourceLoader resourceLoader;
//
//    private ResourcePatternResolver resourcePatternResolver;
//
//    @Autowired
//    public void setupResolver(ApplicationContext applicationContext, AmazonS3 amazonS3) {
//        this.resourcePatternResolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
//    }

    public AmazonSimpleStorageService(ResourceLoader resourceLoader, AmazonS3 amazonS3, ApplicationContext applicationContext,
                                      @Qualifier("customizedThreadPoolExecutor") AsyncTaskExecutor executor) {
        this.resourceLoader = resourceLoader;
        this.resourcePatternResolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
        this.executor = executor;
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
    public void saveResources(Map<URI, File> filesByUrl) {
        List<CompletableFuture<Void>> uploadFutures = filesByUrl.entrySet().stream()
                .map(fileByPath -> CompletableFuture.runAsync(() -> saveResource(fileByPath.getValue(), fileByPath.getKey()), executor))
                .collect(Collectors.toList());
        CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();
    }

    @Override
    public void saveResource(File file, URI url) {
        uploadFileToS3(file, url);
    }

    public void uploadFileToS3(File file, URI s3Url) {
        WritableResource resource = (WritableResource) resourceLoader.getResource(s3Url.toString());
        try (OutputStream outputStream = resource.getOutputStream()) {
            Files.copy(file.toPath(), outputStream);
        } catch (IOException exception) {
            log.error("Cannot upload file: ", exception);
            throw new IllegalArgumentAppException("Error during file upload: ", exception);
        }
    }

    public void downloadS3Object(String s3Url) throws IOException {
        Resource resource = resourceLoader.getResource(s3Url);
        File downloadedS3Object = new File(resource.getFilename());
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, downloadedS3Object.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

}

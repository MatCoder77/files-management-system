package com.awscourse.filesmanagementsystem.domain.file.control.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.awscourse.filesmanagementsystem.infrastructure.asynchronous.CompletableFutures;
import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Arrays;
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

    public AmazonSimpleStorageService(ResourceLoader resourceLoader, AmazonS3 amazonS3, ApplicationContext applicationContext,
                                      @Qualifier("customizedThreadPoolExecutor") AsyncTaskExecutor executor) {
        this.resourceLoader = resourceLoader;
        this.resourcePatternResolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
        this.executor = executor;
    }

    @Override
    public List<Resource> getResources(Collection<URI> urls) {
        List<CompletableFuture<Resource>> resourceFutures = urls.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> getResource(url), executor))
                .collect(Collectors.toList());
        return CompletableFutures.allOf(resourceFutures).join();
    }

    @Override
    public Resource getResource(URI url) {
        return resourceLoader.getResource(url.toString());
    }

    @Override
    public void saveResources(Map<URI, Resource> resourcesByUrl) {
        List<CompletableFuture<Void>> uploadFutures = resourcesByUrl.entrySet().stream()
                .map(fileByPath -> CompletableFuture.runAsync(() -> saveResource(fileByPath.getValue(), fileByPath.getKey()), executor))
                .collect(Collectors.toList());
        CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();
    }

    @Override
    public void saveResource(Resource resourceToSave, URI url) {
        WritableResource s3Resource = (WritableResource) resourceLoader.getResource(url.toString());
        try (OutputStream outputStream = s3Resource.getOutputStream()) {
            resourceToSave.getInputStream().transferTo(outputStream);
        } catch (IOException exception) {
            log.error("Cannot upload file: ", exception);
            throw new IllegalArgumentAppException("Error during file upload: ", exception);
        }
    }

    public List<Resource> getResourcesUnderPath(String s3UrlPattern) throws IOException {
        return Arrays.asList(this.resourcePatternResolver.getResources(s3UrlPattern));
    }

}

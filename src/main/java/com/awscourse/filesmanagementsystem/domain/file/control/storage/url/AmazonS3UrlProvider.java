package com.awscourse.filesmanagementsystem.domain.file.control.storage.url;

import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class AmazonS3UrlProvider implements UrlProvider{

    private final String baseUrl;
    public AmazonS3UrlProvider(@Value("${app.storage}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public URI getUrlForResource(Resource resource, String relativePath) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(getUserPath())
                .path(relativePath)
                .pathSegment(getFileIdentifier(resource))
                .build()
                .toUri();
    }

    private String getUserPath() {
        return UserInfoProvider.requireAuthenticatedUser().getUsername();
    }

    private String getFileIdentifier(Resource resource) {
        return UUID.randomUUID().toString() + getExtensionFilenamePart(resource);
    }

    private String getExtensionFilenamePart(Resource resource) {
        return Optional.ofNullable(FilenameUtils.getExtension(resource.getFilename()))
                .filter(StringUtils::isNoneEmpty)
                .map(extension -> "." + extension)
                .orElse("");
    }

}

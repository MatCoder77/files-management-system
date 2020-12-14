package com.awscourse.filesmanagementsystem.domain.file.control.storage.url;

import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class AmazonS3UrlProvider implements UrlProvider{

    private String baseUrl;
    public AmazonS3UrlProvider(@Value("${app.storage}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public URI getUrlForResource(MultipartFile multipartFile, String relativePath) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(getUserPath())
                .path(relativePath)
                .pathSegment(getFileIdentifier(multipartFile))
                .build()
                .toUri();
    }

    private String getUserPath() {
        return UserInfoProvider.requireAuthenticatedUser().getUsername();
    }

    private String getFileIdentifier(MultipartFile multipartFile) {
        return UUID.randomUUID().toString() + getExtensionFilenamePart(multipartFile);
    }

    private String getExtensionFilenamePart(MultipartFile multipartFile) {
        return Optional.ofNullable(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))
                .filter(StringUtils::isNoneEmpty)
                .map(extension -> "." + extension)
                .orElse("");
    }

}

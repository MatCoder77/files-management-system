package com.awscourse.filesmanagementsystem.domain.file.control.storage.url;

import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

public interface UrlProvider {

    URI getUrlForResource(MultipartFile multipartFile, String relativePath);

}

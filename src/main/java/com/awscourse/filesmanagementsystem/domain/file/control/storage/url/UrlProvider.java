package com.awscourse.filesmanagementsystem.domain.file.control.storage.url;

import org.springframework.core.io.Resource;

import java.net.URI;

public interface UrlProvider {

    URI getUrlForResource(Resource resource, String relativePath);

}

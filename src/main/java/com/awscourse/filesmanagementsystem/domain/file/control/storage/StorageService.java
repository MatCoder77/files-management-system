package com.awscourse.filesmanagementsystem.domain.file.control.storage;


import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

@Transactional
public interface StorageService {

    Resource getResource(URI url);

    Map<URI, Resource> getResources(Collection<URI> url);

    void saveResource(Resource resource, URI url);

    void saveResources(Map<URI, Resource> resourcesByUrl);

}

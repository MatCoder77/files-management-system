package com.awscourse.filesmanagementsystem.domain.file.control.storage;


import org.springframework.core.io.Resource;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StorageService {

    Resource getResource(URI url);

    List<Resource> getResources(Collection<URI> url);

    void saveResource(Resource resource, URI url);

    void saveResources(Map<URI, Resource> resourcesByUrl);

}

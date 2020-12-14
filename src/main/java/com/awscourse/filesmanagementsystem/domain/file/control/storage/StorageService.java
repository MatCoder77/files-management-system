package com.awscourse.filesmanagementsystem.domain.file.control.storage;


import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StorageService {

    File getResource(String relativePath);

    List<File> getResources(Collection<String> relativePaths);

    void saveResource(File file, URI url);

    void saveResources(Map<URI, File> filesByUrl);

}

package com.awscourse.filesmanagementsystem.domain.file.control.storage;


import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StorageService {

    File getResource(String relativePath);

    List<File> getResources(Collection<String> relativePaths);

    String saveResource(File file, String relativePath);

    String saveResources(Map<String, File> filesByRelativePath);

}

package com.awscourse.filesmanagementsystem.domain.file.entity;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class FileResource {

    private final File file;
    private final Resource resource;

}

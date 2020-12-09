package com.awscourse.filesmanagementsystem.domain.file.boundary;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class FilesSearchCriteria {

    private String name;
    private List<String> labels;
    private Long minSize;
    private Long maxSize;
    private String path;
    private Instant minCreatedAt;
    private Instant maxCreatedAt;
    private Instant minLastUpdatedAt;
    private Instant maxLastUpdatedAt;
    private String createdBy;
    private String lastModifiedBy;

}

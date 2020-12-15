package com.awscourse.filesmanagementsystem.api.file;

import com.awscourse.filesmanagementsystem.api.label.LabelDetailsDTO;
import com.awscourse.filesmanagementsystem.api.user.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class FileDetailsDTO {

    private Long id;
    private String name;
    private String path;
    private String fullPath;
    private String description;
    private Long size;
    private List<LabelDetailsDTO> labels;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private UserDTO createdBy;
    private UserDTO lastModifiedBy;

}

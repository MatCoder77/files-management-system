package com.awscourse.filesmanagementsystem.api.file;

import com.awscourse.filesmanagementsystem.api.directory.DirectoryDTO;
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
    private Long size;
    private DirectoryDTO directory;
    private List<LabelDetailsDTO> labels;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private UserDTO createdBy;
    private UserDTO lastModifiedBy;

}

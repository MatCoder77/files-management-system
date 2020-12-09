package com.awscourse.filesmanagementsystem.api.label;

import com.awscourse.filesmanagementsystem.api.user.UserDTO;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LabelDetailsDTO {

    private Long id;
    private String name;
    private String description;
    private LabelType labelType;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private UserDTO createdBy;
    private UserDTO lastModifiedBy;

}

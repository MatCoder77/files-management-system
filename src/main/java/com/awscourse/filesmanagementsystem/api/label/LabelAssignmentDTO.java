package com.awscourse.filesmanagementsystem.api.label;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelAssignmentDTO {

    private Long fileId;
    private List<Long> labelIds;

}

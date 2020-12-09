package com.awscourse.filesmanagementsystem.api.label;

import com.awscourse.filesmanagementsystem.domain.label.entity.LabelType;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class LabelDTO {

    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private LabelType labelType;

}

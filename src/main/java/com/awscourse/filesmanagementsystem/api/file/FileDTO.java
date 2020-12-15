package com.awscourse.filesmanagementsystem.api.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String path;

    private String description;

    @NotBlank
    private URI url;

    @NotNull
    private List<Long> labels;

}

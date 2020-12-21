package com.awscourse.filesmanagementsystem.api.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.net.URI;

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

}

package com.awscourse.filesmanagementsystem.api.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileInfoDTO {

    private Long id;
    private String name;
    private Long size;
    private Long directory;

}

package com.awscourse.filesmanagementsystem.api.file;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FilesSearchResultDTO {

    private List<FileDetailsDTO> results;
    private Long totalSize;
    private Integer pageSize;
    private Integer pageNumber;

}

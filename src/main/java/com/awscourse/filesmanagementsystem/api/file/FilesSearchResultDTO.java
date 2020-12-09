package com.awscourse.filesmanagementsystem.api.file;

import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.util.List;

@Data
@Builder
public class FilesSearchResultDTO {

    private List<FileInfoDTO> results;
    private Long totalSize;
    private Long pageSize;
    private Long pageNumber;
    private URI firstPage;
    private URI previousPage;
    private URI nextPage;
    private URI lastPage;
    private URI currentPage;

}

package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.file.boundary.FilesSearchCriteria;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchFileRepository {

    Page<File> searchFilesByCriteria(FilesSearchCriteria searchCriteria, Pageable pageable);

}

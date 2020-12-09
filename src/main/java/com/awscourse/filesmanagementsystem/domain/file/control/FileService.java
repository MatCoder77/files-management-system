package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.file.boundary.FilesSearchCriteria;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private

    public Page<File> searchFilesByCriteria(FilesSearchCriteria searchCriteria, Pageable pageable) {
        return fileRepository.searchFilesByCriteria(searchCriteria, pageable);
    }

    public List<File> getFilesByIds(Collection<Long> ids) {
        return fileRepository.findAllById(ids);
    }

}

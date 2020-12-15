package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long>, SearchFileRepository {

    List<File> findAllByFullPathInAndIdNotIn(Collection<String> fullPaths, Collection<Long> ids);

}

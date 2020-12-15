package com.awscourse.filesmanagementsystem.domain.file.boundary;

import com.awscourse.filesmanagementsystem.api.file.FileDTO;
import com.awscourse.filesmanagementsystem.api.file.FileDetailsDTO;
import com.awscourse.filesmanagementsystem.api.file.FileUploadResponseDTO;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.UploadInfo;
import com.awscourse.filesmanagementsystem.domain.label.boundary.LabelMapper;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;
import com.awscourse.filesmanagementsystem.domain.user.boundary.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMapper {

    private final LabelMapper labelMapper;
    private final UserMapper userMapper;

    public List<FileDetailsDTO> mapToFileDetailsDTOs(Collection<File> files) {
        return files.stream()
                .map(this::mapToFileDetailsDTO)
                .collect(Collectors.toList());
    }

    public FileDetailsDTO mapToFileDetailsDTO(File file) {
        return FileDetailsDTO.builder()
                .id(file.getId())
                .name(file.getName())
                .path(file.getPath())
                .fullPath(file.getFullPath())
                .description(file.getDescription())
                .size(file.getSize())
                .labels(labelMapper.mapToLabelDetailDTOs(file.getLabels()))
                .createdAt(file.getCreatedAt())
                .createdBy(userMapper.mapToUserDTO(file.getCreatedBy()))
                .lastModifiedAt(file.getUpdatedAt())
                .lastModifiedBy(userMapper.mapToUserDTO(file.getUpdatedBy()))
                .build();
    }

    public List<File> mapToFile(Collection<FileDTO> fileDTOs) {
        return fileDTOs.stream()
                .map(this::mapToFile)
                .collect(Collectors.toList());
    }

    public File mapToFile(FileDTO fileDTO) {
        return File.builder()
                .id(fileDTO.getId())
                .name(fileDTO.getName())
                .path(fileDTO.getPath())
                .fullPath(getFullPath(fileDTO))
                .description(fileDTO.getDescription())
                .url(fileDTO.getUrl())
                .build();
    }

    public String getFullPath(FileDTO fileDTO) {
        return UriComponentsBuilder.fromUriString(fileDTO.getPath())
                .pathSegment(fileDTO.getName())
                .toString();
    }

    public List<Resource> mapToResources(MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles)
                .map(MultipartFile::getResource)
                .collect(Collectors.toList());
    }

    public List<FileUploadResponseDTO> mapToFileUploadResponseDTOs(Collection<UploadInfo> uploadInfo, Map<URI, List<LabelCalculationResult>> labelsByUri) {
        return uploadInfo.stream()
                .map(info -> mapToFileUploadResponseDTO(info, labelsByUri.getOrDefault(info.getUrl(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private FileUploadResponseDTO mapToFileUploadResponseDTO(UploadInfo uploadInfo, Collection<LabelCalculationResult> labels) {
        if (uploadInfo == null) {
            return null;
        }
        return new FileUploadResponseDTO(uploadInfo.getFilename(), uploadInfo.getUrl(), labelMapper.mapToLabelSuggestionDTOs(labels));
    }

}

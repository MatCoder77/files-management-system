package com.awscourse.filesmanagementsystem.domain.file.boundary;

import com.awscourse.filesmanagementsystem.api.file.FileUploadResponseDTO;
import com.awscourse.filesmanagementsystem.domain.file.entity.UploadInfo;
import com.awscourse.filesmanagementsystem.domain.label.boundary.LabelMapper;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMapper {

    private final LabelMapper labelMapper;

    public List<FileUploadResponseDTO> mapToFileUploadResponseDTOs(Collection<UploadInfo> uploadInfo, Map<URI, List<LabelCalculationResult>> labelsByUri) {
        return uploadInfo.stream()
                .map(info -> mapToFileUploadResponseDTO(info, labelsByUri.getOrDefault(info.getUrl(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private FileUploadResponseDTO mapToFileUploadResponseDTO(UploadInfo uploadInfo, List<LabelCalculationResult> labels) {
        if (uploadInfo == null) {
            return null;
        }
        return new FileUploadResponseDTO(uploadInfo.getFilename(), uploadInfo.getUrl(), labelMapper.mapToLabelSuggestionDTOs(labels));
    }

//    public List<FileDetailsDTO> mapToFileDetailsDTOs(Collection<File> files) {
//        return files.stream()
//                .map()
//    }
//
//    public FileDetailsDTO mapToFileDetailsDTO(File file) {
//        return FileDetailsDTO.builder()
//                .id(file.getId())
//                .name(file.getName())
//                .size(file.getSize())
//                .labels(file.)
//    }

}

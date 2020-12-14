package com.awscourse.filesmanagementsystem.api.file;

import com.awscourse.filesmanagementsystem.api.label.LabelSuggestionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponseDTO {

    private String filename;
    private URI url;
    private List<LabelSuggestionDTO> calculatedLabels;

}

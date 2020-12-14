package com.awscourse.filesmanagementsystem.api.label;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelSuggestionDTO {
    
    private String name;
    private float confidence;
    
}

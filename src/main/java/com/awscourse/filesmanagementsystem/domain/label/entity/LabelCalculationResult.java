package com.awscourse.filesmanagementsystem.domain.label.entity;

import lombok.Data;

@Data
public class LabelCalculationResult {

    private final String name;
    private final float confidence;

}

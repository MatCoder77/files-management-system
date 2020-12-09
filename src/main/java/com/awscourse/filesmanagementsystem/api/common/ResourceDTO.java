package com.awscourse.filesmanagementsystem.api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.net.URI;

@Data
@Builder
@AllArgsConstructor
public class ResourceDTO {

    private Long id;
    private String identifier;
    private URI uri;

}

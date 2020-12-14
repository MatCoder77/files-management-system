package com.awscourse.filesmanagementsystem.domain.file.entity;

import lombok.Data;

import java.net.URI;

@Data
public class UploadInfo {

    private final String filename;
    private final URI url;

}

package com.awscourse.filesmanagementsystem.domain.file.boundary;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;

@Component
@RequiredArgsConstructor
public class MediaTypeResolver {

    private final ServletContext servletContext;

    public MediaType getMediaTypeForFile(String fileName) {
        String mineType = servletContext.getMimeType(fileName);
        try {
            return MediaType.parseMediaType(mineType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

}

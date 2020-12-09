package com.awscourse.filesmanagementsystem.domain.file.boundary;

import com.awscourse.filesmanagementsystem.api.file.FileDetailsDTO;
import com.awscourse.filesmanagementsystem.api.file.FileInfoDTO;
import com.awscourse.filesmanagementsystem.api.file.FileUploadResponseDTO;
import com.awscourse.filesmanagementsystem.domain.file.control.FileService;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfo;
import com.awscourse.filesmanagementsystem.infrastructure.security.annotation.LoggedUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.awscourse.filesmanagementsystem.domain.file.boundary.FileController.FILE_RESOURCE;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.IDS;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.IDS_PATH;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID_PATH;

@Api(tags = "Files")
@Controller
@RequestMapping(FILE_RESOURCE)
@RequiredArgsConstructor
public class FileController {

    public static final String FILE_RESOURCE = "/api/files";

    private final FileMapper fileMapper;
    private final FileService fileService;

    @ApiOperation(value = "${api.files.searchFiles.value}", notes = "${api.files.searchFiles.notes}")
    @GetMapping("/search")
    public List<FileInfoDTO> searchFiles(@Valid FilesSearchCriteria searchCriteria, @Valid  Pageable pageable, @LoggedUser UserInfo userInfo) {
        return Collections.emptyList();
    }

    @ApiOperation(value = "${api.files.getFilesByIds.value}", notes = "${api.files.getFilesByIds.notes}")
    @GetMapping(IDS_PATH)
    public List<FileDetailsDTO> getFilesByIds(@PathVariable(IDS) Collection<Long> ids, @LoggedUser UserInfo userInfo) {
        List<File> filesByIds = fileService.getFilesByIds(ids);
        return null;
    }

    @ApiOperation(value = "${api.files.getFilesByDirectoryId.value}", notes = "${api.files.getFilesByDirectoryId.notes}")
    @GetMapping("/directory" + ID_PATH)
    public List<FileDetailsDTO> getFilesByDirectoryId(@PathVariable(ID) Long id) {
        return null;
    }

    @ApiOperation(value = "${api.files.downloadFile.value}", notes = "${api.files.downloadFile.notes}")
    @GetMapping(value = "/download" + ID, produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable(ID) Long id) {
        return null;
//        BucketObject file = simpleStorageService.getFile(bucketName, filename);
//        ByteArrayResource resource = new ByteArrayResource(file.getContent());
//        return ResponseEntity
//                .ok()
//                .contentLength(file.getContent().length)
//                .header("Content-type", "application/octet-stream")
//                .header("Content-disposition", "attachment; filename=\"" + file.getKey() + "\"")
//                .body(resource);
    }

    @ApiOperation(value = "${api.files.downloadZip.value}", notes = "${api.files.downloadZip.notes}")
    @GetMapping(value = "/download-zip", produces="application/zip")
    public void downloadZippedFilesByIds(@RequestParam(IDS) List<Long> ids, HttpServletResponse response) throws IOException {
//        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
//        for (String fileName : name) {
//            FileSystemResource resource = new FileSystemResource(fileBasePath + fileName);
//            ZipEntry zipEntry = new ZipEntry(resource.getFilename());
//            zipEntry.setSize(resource.contentLength());
//            zipOut.putNextEntry(zipEntry);
//            StreamUtils.copy(resource.getInputStream(), zipOut);
//            zipOut.closeEntry();
//        }
//        zipOut.finish();
//        zipOut.close();
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");
    }

    @ApiOperation(value = "${api.files.uploadFiles.value}", notes = "${api.files.uploadFiles.notes}")
    @PostMapping(value = "/upload")
    public List<FileUploadResponseDTO> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        return Collections.emptyList();
    }

}

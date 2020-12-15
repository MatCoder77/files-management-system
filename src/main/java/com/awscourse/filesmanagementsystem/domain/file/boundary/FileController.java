package com.awscourse.filesmanagementsystem.domain.file.boundary;

import com.awscourse.filesmanagementsystem.api.common.ResourceDTO;
import com.awscourse.filesmanagementsystem.api.file.FileDTO;
import com.awscourse.filesmanagementsystem.api.file.FileDetailsDTO;
import com.awscourse.filesmanagementsystem.api.file.FileInfoDTO;
import com.awscourse.filesmanagementsystem.api.file.FileUploadResponseDTO;
import com.awscourse.filesmanagementsystem.domain.file.control.FileService;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.UploadInfo;
import com.awscourse.filesmanagementsystem.domain.label.control.LabelCalculationService;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfo;
import com.awscourse.filesmanagementsystem.infrastructure.security.annotation.LoggedUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final LabelCalculationService labelCalculationService;

    @ApiOperation(value = "${api.files.searchFiles.value}", notes = "${api.files.searchFiles.notes}")
    @GetMapping("/search")
    public List<FileInfoDTO> searchFiles(@Valid FilesSearchCriteria searchCriteria, @Valid  Pageable pageable, @LoggedUser UserInfo userInfo) {
        return Collections.emptyList();
    }

    @ApiOperation(value = "${api.files.getFilesByIds.value}", notes = "${api.files.getFilesByIds.notes}")
    @GetMapping(IDS_PATH)
    public List<FileDetailsDTO> getFilesByIds(@PathVariable(IDS) Collection<Long> ids, @LoggedUser UserInfo userInfo) {
        List<File> files = fileService.getFilesByIds(ids);
        return fileMapper.mapToFileDetailsDTOs(files);
    }

    @ApiOperation(value = "${api.files.getFilesByDirectoryId.value}", notes = "${api.files.getFilesByDirectoryId.notes}")
    @GetMapping("/directory" + ID_PATH)
    public List<FileDetailsDTO> getFilesByDirectoryId(@PathVariable(ID) Long id) {
        return null;
    }

    @ApiOperation(value = "${api.files.createFiles.value}", notes = "${api.files.createFiles.notes}")
    @PostMapping
    public List<ResourceDTO> createFiles(@Valid @RequestBody List<FileDTO> fileDTOs) {
        List<File> files = fileMapper.mapToFile(fileDTOs);
        List<File> createdFiles = fileService.createFiles(files);
        //List<Label>
        return Collections.emptyList();
    }

    @ApiOperation(value = "${api.files.downloadFile.value}", notes = "${api.files.downloadFile.notes}")
    @GetMapping(value = "/download" + ID, produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<Resource> downloadFile(@PathVariable(ID) Long id, HttpServletRequest request) {
        Resource resource = fileService.downloadResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileUploadResponseDTO>> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                                   @RequestParam(value = "path", defaultValue = "") String path,
                                                   @RequestParam(value = "maxLabelsPerFile", defaultValue = "5") int maxLabelsPerFile,
                                                   @RequestParam(value = "minConfidence", defaultValue = "80") float minConfidence) {
        List<Resource> resources = fileMapper.mapToResources(files);
        List<UploadInfo> uploadInfo = fileService.uploadResources(resources, path);
        Map<URI, List<LabelCalculationResult>> suggestedLabelsByUri =
                labelCalculationService.calculateLabelsForResources(getUrls(uploadInfo), maxLabelsPerFile, minConfidence);
        List<FileUploadResponseDTO> fileUploadResponseDTOs =
                fileMapper.mapToFileUploadResponseDTOs(uploadInfo, suggestedLabelsByUri);
        return ResponseEntity.status(HttpStatus.OK)
                .body(fileUploadResponseDTOs);
    }

    private List<URI> getUrls(Collection<UploadInfo> uploadInfo) {
        return uploadInfo.stream()
                .map(UploadInfo::getUrl)
                .collect(Collectors.toList());
    }

}

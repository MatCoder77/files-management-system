package com.awscourse.filesmanagementsystem.domain.file.boundary;

import com.awscourse.filesmanagementsystem.api.common.ResourceDTO;
import com.awscourse.filesmanagementsystem.api.common.ResponseDTO;
import com.awscourse.filesmanagementsystem.api.file.FileDTO;
import com.awscourse.filesmanagementsystem.api.file.FileDetailsDTO;
import com.awscourse.filesmanagementsystem.api.file.FileUploadResponseDTO;
import com.awscourse.filesmanagementsystem.api.file.FilesSearchResultDTO;
import com.awscourse.filesmanagementsystem.api.label.LabelAssignmentDTO;
import com.awscourse.filesmanagementsystem.domain.file.control.FileService;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.FileResource;
import com.awscourse.filesmanagementsystem.domain.file.entity.UploadInfo;
import com.awscourse.filesmanagementsystem.domain.label.control.LabelCalculationService;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;
import com.awscourse.filesmanagementsystem.domain.labelassignment.control.LabelAssignmentService;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfo;
import com.awscourse.filesmanagementsystem.infrastructure.security.annotation.LoggedUser;
import com.awscourse.filesmanagementsystem.infrastructure.transform.TransformUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.awscourse.filesmanagementsystem.domain.file.boundary.FileController.FILE_RESOURCE;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.IDS;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.IDS_PATH;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID_PATH;

@Api(tags = "Files")
@RestController
@RequestMapping(FILE_RESOURCE)
@RequiredArgsConstructor
public class FileController {

    public static final String FILE_RESOURCE = "/api/files";
    private static final String DETECT_CONTENT_TYPE = "detectContentType";

    private final FileMapper fileMapper;
    private final FileService fileService;
    private final LabelCalculationService labelCalculationService;
    private final LabelAssignmentService labelAssignmentService;
    private final MediaTypeResolver mediaTypeResolver;

    @ApiOperation(value = "${api.files.searchFiles.value}", notes = "${api.files.searchFiles.notes}")
    @GetMapping("/search")
    public FilesSearchResultDTO searchFiles(@Valid FilesSearchCriteria searchCriteria, @Valid  Pageable pageable) {
        Page<File> filePage = fileService.searchFilesByCriteria(searchCriteria, pageable);
        return fileMapper.mapToFilesSearchResultDTO(filePage);
    }

    @ApiOperation(value = "${api.files.getFilesByIds.value}", notes = "${api.files.getFilesByIds.notes}")
    @GetMapping(IDS_PATH)
    public List<FileDetailsDTO> getFilesByIds(@PathVariable(IDS) Collection<Long> ids) {
        List<File> files = fileService.getFilesByIds(ids);
        return fileMapper.mapToFileDetailsDTOs(files);
    }

    @ApiOperation(value = "${api.files.getFilesByDirectoryId.value}", notes = "${api.files.getFilesByDirectoryId.notes}")
    @GetMapping("/directory" + ID_PATH)
    public List<FileDetailsDTO> getFilesByDirectoryId(@PathVariable(ID) Long id) {
        return Collections.emptyList();
    }

    @ApiOperation(value = "${api.files.createFiles.value}", notes = "${api.files.createFiles.notes}")
    @PostMapping
    public List<ResourceDTO> createFiles(@Valid @RequestBody List<FileDTO> fileDTOs) {
        List<File> files = fileMapper.mapToFile(fileDTOs);
        List<File> createdFiles = fileService.createFiles(files);
        return fileMapper.mapToResourceDTOs(createdFiles);
    }

    @ApiOperation(value = "${api.files.createLabelAssignmentForFiles.value}", notes = "${api.files.createLabelAssignmentForFiles.notes}")
    @PostMapping("/add-labels")
    public ResponseDTO<Boolean> createLabelAssignmentForFiles(@Valid @RequestBody List<LabelAssignmentDTO> labelAssignmentDTOs) {
        Map<Long, List<Long>> idsOfLabelsToAssignByFileId =
                TransformUtils.transformToMap(labelAssignmentDTOs, LabelAssignmentDTO::getFileId, LabelAssignmentDTO::getLabelIds);
        labelAssignmentService.createLabelAssignments(idsOfLabelsToAssignByFileId);
        return new ResponseDTO<>(true, "Labels assigned successfully");
    }

    @ApiOperation(value = "${api.files.deleteLabelAssignmentForFiles.value}", notes = "${api.files.deleteLabelAssignmentForFiles.notes}")
    @DeleteMapping("/remove-labels")
    public ResponseDTO<Boolean> deleteLabelAssignmentForFiles(@Valid @RequestBody List<LabelAssignmentDTO> labelAssignmentDTOs) {
        Map<Long, List<Long>> idsOfLabelsToRemoveByFileId =
                TransformUtils.transformToMap(labelAssignmentDTOs, LabelAssignmentDTO::getFileId, LabelAssignmentDTO::getLabelIds);
        labelAssignmentService.deleteLabelAssignments(idsOfLabelsToRemoveByFileId);
        return new ResponseDTO<>(true, "Labels removed from files successfully");
    }

    @ApiOperation(value = "${api.files.updateFiles.value}", notes = "${api.files.updateFiles.notes}")
    @PutMapping
    public List<ResourceDTO> updateFiles(@Valid @RequestBody List<FileDTO> fileDTOs, @ApiIgnore @LoggedUser UserInfo userInfo) {
        List<File> filesToUpdate = fileMapper.mapToFile(fileDTOs);
        fileService.updateFiles(filesToUpdate, userInfo.getId());
        return fileMapper.mapToResourceDTOs(filesToUpdate);
    }

    @ApiOperation(value = "${api.files.downloadFile.value}", notes = "${api.files.downloadFile.notes}")
    @GetMapping(value = "/download" + ID_PATH)
    public ResponseEntity<Resource> downloadFile(@PathVariable(ID) Long id,
                                                 @RequestParam(value = DETECT_CONTENT_TYPE, defaultValue = "false") boolean detectContentType) {
        FileResource fileResource = fileService.downloadResource(id);
        File file = fileResource.getFile();
        Resource resource = fileResource.getResource();
        return ResponseEntity.ok()
                .contentType(detectContentType ? mediaTypeResolver.getMediaTypeForFile(FilenameUtils.getName(resource.getFilename())) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    @ApiOperation(value = "${api.files.downloadZip.value}", notes = "${api.files.downloadZip.notes}")
    @GetMapping(value = "/download-zip" + IDS_PATH, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadZippedFilesByIds(@PathVariable(IDS) List<Long> ids,
                                         @RequestParam("downloadedFileName") Optional<String> downloadedFileName,
                                         HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadedFileName.orElse("files") + ".zip" + "\"");
        List<FileResource> fileResources = fileService.downloadResources(ids);
        try(ZipOutputStream outputStream = new ZipOutputStream(response.getOutputStream())) {
            for (FileResource fileResource : fileResources) {
                copyResourceToZipOutputStream(outputStream, fileResource);
            }
        }
    }

    private void copyResourceToZipOutputStream(ZipOutputStream outputStream, FileResource fileResource) throws IOException {
        outputStream.putNextEntry(mapToZipEntry(fileResource.getFile()));
        fileResource.getResource().getInputStream().transferTo(outputStream);
        outputStream.closeEntry();
    }

    private ZipEntry mapToZipEntry(File file) {
        ZipEntry zipEntry = new ZipEntry(file.getFullPath());
        zipEntry.setSize(file.getSize());
        return zipEntry;
    }

    @ApiOperation(value = "${api.files.uploadFiles.value}", notes = "${api.files.uploadFiles.notes}")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileUploadResponseDTO> uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                                   @RequestParam(value = "path", defaultValue = "") String path,
                                                   @RequestParam(value = "maxLabelsPerFile", defaultValue = "5") int maxLabelsPerFile,
                                                   @RequestParam(value = "minConfidence", defaultValue = "80") float minConfidence) {
        List<Resource> resources = fileMapper.mapToResources(files);
        List<UploadInfo> uploadInfo = fileService.uploadResources(resources, path);
        Map<URI, List<LabelCalculationResult>> suggestedLabelsByUri =
                labelCalculationService.calculateLabelsForResources(getUrls(uploadInfo), maxLabelsPerFile, minConfidence);
        return fileMapper.mapToFileUploadResponseDTOs(uploadInfo, suggestedLabelsByUri);
    }

    private List<URI> getUrls(Collection<UploadInfo> uploadInfo) {
        return uploadInfo.stream()
                .map(UploadInfo::getUrl)
                .collect(Collectors.toList());
    }

}

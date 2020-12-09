package com.awscourse.filesmanagementsystem.domain.label.boundary;

import com.awscourse.filesmanagementsystem.api.common.ResourceDTO;
import com.awscourse.filesmanagementsystem.api.common.ResponseDTO;
import com.awscourse.filesmanagementsystem.api.label.LabelDTO;
import com.awscourse.filesmanagementsystem.api.label.LabelDetailsDTO;
import com.awscourse.filesmanagementsystem.domain.label.control.LabelService;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfo;
import com.awscourse.filesmanagementsystem.infrastructure.security.annotation.LoggedUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.IDS;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.IDS_PATH;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID_PATH;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID;

@Api(tags = "Labels")
@RestController
@RequestMapping(LabelController.LABEL_RESOURCE)
@RequiredArgsConstructor
public class LabelController {

    public static final String LABEL_RESOURCE = "/api/labels";
    public static final String MY_LABELS_PATH = "/my";
    private final LabelMapper labelMapper;
    private final LabelService labelService;

    @ApiOperation(value = "${api.labels.getLabelsByIds.value}", notes = "${api.labels.getLabelsByIds.notes}")
    @GetMapping(IDS_PATH)
    public List<LabelDetailsDTO> getLabelsByIds(@PathVariable(IDS) List<Long> ids) {
        List<Label> foundLabels = labelService.getLabelsByIds(ids);
        return labelMapper.mapToLabelDetailDTOs(foundLabels);
    }

    @ApiOperation(value = "${api.labels.getMyLabels.value}", notes = "${api.labels.getMyLabels.notes}")
    @GetMapping(MY_LABELS_PATH)
    public List<LabelDetailsDTO> getMyLabels(@ApiIgnore @LoggedUser UserInfo userInfo) {
        List<Label> labelsCreatedByUser = labelService.getLabelsCreatedByUser(userInfo.getId());
        return labelMapper.mapToLabelDetailDTOs(labelsCreatedByUser);
    }

    @ApiOperation(value = "${api.labels.createLabels.value}", notes = "${api.labels.createLabels.notes}")
    @PostMapping
    public List<ResourceDTO> createLabels(@Valid @RequestBody List<LabelDTO> labelDTOs) {
        List<Label> labelsToCreate = labelMapper.mapToLabels(labelDTOs);
        List<Label> createdLabels = labelService.createLabels(labelsToCreate);
        return labelMapper.mapToResourceDTOs(createdLabels);
    }

    @ApiOperation(value = "${api.labels.updateLabels.value}", notes = "${api.labels.updateLabels.notes}")
    @PutMapping
    public List<ResourceDTO> updateLabels(@Valid @RequestBody List<LabelDTO> labelDTOs, @ApiIgnore @LoggedUser UserInfo userInfo) {
        List<Label> labelsToUpdate = labelMapper.mapToLabels(labelDTOs);
        labelService.updateLabels(labelsToUpdate, userInfo.getId());
        return labelMapper.mapToResourceDTOs(labelsToUpdate);
    }

    @ApiOperation(value = "${api.labels.deleteLabels.value}", notes = "${api.labels.deleteLabels.notes}")
    @DeleteMapping(IDS_PATH)
    public ResponseDTO<Boolean> deleteLabels(@PathVariable(IDS) List<Long> ids, @ApiIgnore @LoggedUser UserInfo userInfo) {
        labelService.deleteLabels(ids, userInfo.getId());
        return new ResponseDTO<>(true, "Labels deleted successfully");
    }

}

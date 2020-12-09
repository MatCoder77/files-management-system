package com.awscourse.filesmanagementsystem.api.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class ResponseDTO<T> {

    @NotNull
    private T response;
    private String message;

}

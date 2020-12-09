package com.awscourse.filesmanagementsystem.infrastructure.exception;

import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Set;

@UtilityClass
public class ExceptionUtils {

    private static final String OBJECT_NOT_FOUND_MSG = "There is no {0} with id {1}";

    public IllegalArgumentAppException getObjectNotFoundException(Class<?> objectType, Long id) {
        return new IllegalArgumentAppException(MessageFormat.format(OBJECT_NOT_FOUND_MSG, objectType.getSimpleName(), String.valueOf(id)));
    }

    public IllegalArgumentAppException getObjectNotFoundException(Class<?> objectType, Set<Long> ids) {
        return new IllegalArgumentAppException(MessageFormat.format(OBJECT_NOT_FOUND_MSG, objectType.getSimpleName(), StringUtils.join(ids)));
    }

}

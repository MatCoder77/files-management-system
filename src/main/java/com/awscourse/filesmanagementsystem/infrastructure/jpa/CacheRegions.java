package com.awscourse.filesmanagementsystem.infrastructure.jpa;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheRegions {

    public static final String USER_ENTITY_CACHE = "userCache";
    public static final String USER_NATURAL_ID_CACHE = "userCache##NaturalId";

}

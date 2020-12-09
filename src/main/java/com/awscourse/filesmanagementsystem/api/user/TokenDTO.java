package com.awscourse.filesmanagementsystem.api.user;

import lombok.Data;

@Data
public class TokenDTO {

    private String accessToken;
    private String tokenType = "Bearer";

    public TokenDTO(String accessToken) {
        this.accessToken = accessToken;
    }

}

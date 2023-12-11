package org.talend.components.test.validationserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Token {

    private String access_token;
    private String token_type;
    private long expires_in;

}

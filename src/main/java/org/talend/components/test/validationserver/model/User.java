package org.talend.components.test.validationserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {

    private Integer id;
    private String name;
    private Boolean active;

}

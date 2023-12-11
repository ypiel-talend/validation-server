package org.talend.components.test.validationserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ControllerError {

    private String message;

    private String cause;

}

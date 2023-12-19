package org.talend.components.test.validationserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoAuthController {

    @GetMapping("/ping")
    public String clientCredentialsToken(){
        return "pong.";
    }
}

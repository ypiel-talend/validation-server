package org.talend.components.test.validationserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

@RestController
public class NoAuthController {

    public final static String PONG = "pong.";


    @GetMapping(value = "/ping", produces = "text/plain")
    public String pingTextPlain() {
        return PONG;
    }

    @GetMapping(value = "/ping", produces = "application/json")
    public Map<String, String> pingJson() {
        return Collections.singletonMap("message", PONG);
    }

    @GetMapping("/loadfile")
    public String loadFile(@RequestParam(name="file", required = false) String file) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
        return jsonContent;
    }
}

package org.talend.components.test.validationserver;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@Log4j2
public class NoAuthController {

    public final static String PONG = "pong.";
    public final static int DEFAULT_PAGINATION_OFFSET = 0;
    public final static int DEFAULT_PAGINATION_LIMIT = 10;
    public final static int DEFAULT_PAGINATION_TOTAL = 100;

    private static int RETRY_503_ATTEMPTS = 0;
    private static int RETRY_503_ATTEMPTS_SUCCESS = Integer.valueOf(System.getProperty("validation-server.noauth-controller.retry-503-attempts-success", "4"));

    private static int RETRY_TIMEOUT_ATTEMPTS = 0;
    private static int RETRY_TIMEOUT_ATTEMPTS_SUCCESS = Integer.valueOf(System.getProperty("validation-server.noauth-controller.retry-timeout-attempts-success", "4"));
    private static long RETRY_TIMEOUT_ATTEMPTS_DELAY = Long.valueOf(System.getProperty("validation-server.noauth-controller.retry-timeout-attempts-delay", "3000"));


    @GetMapping(value = "/ping", produces = "text/plain")
    public String pingTextPlain() {
        return PONG;
    }

    @GetMapping(value = "/ping", produces = "application/json")
    public Map<String, String> pingJson() {
        return Collections.singletonMap("message", PONG);
    }

    @PostMapping(value = "/post", produces = "application/json")
    public Map<String, String> postPlainText(@RequestBody String payload) throws IOException {
        log.info(String.format("Received Payload:\n%s\n--\nEND.\n", payload));

        return Collections.singletonMap("post_body", payload);
    }

    @PostMapping(value="/post", produces = "text/plain")
    public String postJSON(@RequestBody String payload) throws IOException {
        log.info(String.format("Received Payload:\n%s\n--\nEND.\n", payload));

        return payload;
    }

    @GetMapping(value = "/retry503")
    public ResponseEntity<Map<String, String>> retry503() {
        if (++RETRY_503_ATTEMPTS >= RETRY_503_ATTEMPTS_SUCCESS) {
            RETRY_503_ATTEMPTS = 0;
            return ResponseEntity.ok(Collections.singletonMap("success", "true"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Collections.singletonMap("error", String.format("You have still to retry '%s' times.", (RETRY_503_ATTEMPTS_SUCCESS - RETRY_503_ATTEMPTS))));
        }
    }

    @GetMapping(value = "/retryTimeout")
    public ResponseEntity<Map<String, String>> retryTimeout() {
        if (++RETRY_TIMEOUT_ATTEMPTS >= RETRY_TIMEOUT_ATTEMPTS_SUCCESS) {
            RETRY_TIMEOUT_ATTEMPTS = 0;
            return ResponseEntity.ok(Collections.singletonMap("message", "success"));
        } else {
            try {
                Thread.sleep(RETRY_TIMEOUT_ATTEMPTS_DELAY);
            } catch (InterruptedException e) {
                log.error("Timeout sleep failed.", e);
            }
            return ResponseEntity
                    .ok(Collections.singletonMap("message", String.format("Wait for timeout will be disable in '%s' attempts.", (RETRY_TIMEOUT_ATTEMPTS_SUCCESS - RETRY_TIMEOUT_ATTEMPTS))));
        }
    }

    @GetMapping(value = "/paginateNestedArray", produces = "application/json")
    public Map<String, Object> paginateNestedArray(@RequestParam(name = "offset", required = false) Integer offset,
                                                   @RequestParam(name = "limit", required = false) Integer limit,
                                                   @RequestParam(name = "total", required = false) Integer total) {
        List<Element> elements = paginate(offset, limit, total);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("offset", offset);
        result.put("limit", limit);
        result.put("total", total);
        result.put("size", elements.size());
        result.put("elements", elements);

        return result;
    }

    @GetMapping(value = "/paginate", produces = "application/json")
    public List<Element> paginate(@RequestParam(name = "offset", required = false) Integer offset,
                                  @RequestParam(name = "limit", required = false) Integer limit,
                                  @RequestParam(name = "total", required = false) Integer total) {
        if (offset == null) {
            offset = DEFAULT_PAGINATION_OFFSET;
        }
        if (limit == null) {
            limit = DEFAULT_PAGINATION_LIMIT;
        }
        if (total == null) {
            total = DEFAULT_PAGINATION_TOTAL;
        }
        System.out.printf("%s / %s / %s\n", offset, limit, total);

        List<Element> result = IntStream.rangeClosed(1, total).mapToObj(i -> new Element(i)).collect(Collectors.toList());

        if (offset >= total) {
            return Collections.emptyList();
        }

        int toIndex = offset + limit;
        result = result.subList(offset, toIndex > total ? total : toIndex);
        return result;
    }

    @RequestMapping("/status")
    public ResponseEntity<Map<String, String>> status(@RequestParam(name = "status") int status, @RequestParam(name = "message") String message) {
        Map<String, String> body = new HashMap<>();
        body.put("status", String.valueOf(status));
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    @Data
    public final static class Element {
        private final int id;
        private final String label;
        private final Element nested;

        private final String nestedAsJson;

        public Element(int id) {
            this.id = id;
            this.label = String.format("element_%s", id);
            if (id >= 0) {
                this.nested = new Element(id * -1);
                this.nestedAsJson = String.format("{\"id\": %s, \"label\": \"nested json %s\"}", id, id);
            } else {
                this.nested = null;
                this.nestedAsJson = null;
            }
        }
    }
}

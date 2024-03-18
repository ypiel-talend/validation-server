package org.talend.components.test.validationserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class NoAuthController {

    public final static String PONG = "pong.";
    public final static int DEFAULT_PAGINATION_OFFSET = 0;
    public final static int DEFAULT_PAGINATION_LIMIT = 10;
    public final static int DEFAULT_PAGINATION_TOTAL = 100;


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

    @PostMapping(value = "/post", produces = "application/json")
    public Map<String, String> postPlainText(@RequestBody String payload) throws IOException {
        System.out.printf("Received Payload:\n%s\n--\nEND.\n", payload);

        return Collections.singletonMap("post_body", payload);
    }

    @PostMapping(value="/post", produces = "text/plain")
    public String postJSON(@RequestBody String payload) throws IOException {
        System.out.printf("Received Payload:\n%s\n--\nEND.\n", payload);

        return payload;
    }

    @GetMapping(value = "/paginateNestedArray", produces = "application/json")
    public Map<String, Object> paginateNestedArray(@RequestParam(name="offset", required = false) Integer offset,
                                  @RequestParam(name="limit", required = false) Integer limit,
                                  @RequestParam(name="total", required = false) Integer total){
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
    public List<Element> paginate(@RequestParam(name="offset", required = false) Integer offset,
                                        @RequestParam(name="limit", required = false) Integer limit,
                                        @RequestParam(name="total", required = false) Integer total){
        if(offset == null){
            offset = DEFAULT_PAGINATION_OFFSET;
        }
        if(limit == null){
            limit = DEFAULT_PAGINATION_LIMIT;
        }
        if(total == null){
            total = DEFAULT_PAGINATION_TOTAL;
        }
        System.out.printf("%s / %s / %s\n", offset, limit, total);

        List<Element> result = IntStream.rangeClosed(1, total).mapToObj(i -> new Element(i, String.format("element_%s", i))).collect(Collectors.toList());

        if(offset >= total){
            return Collections.emptyList();
        }

        int toIndex = offset + limit;
        result = result.subList(offset, toIndex > total ? total : toIndex);
        return result;
    }

    @Data
    @AllArgsConstructor
    public final static class Element{
        private final int id;
        private final String label;
    }
}
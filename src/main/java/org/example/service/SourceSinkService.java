package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.model.SinkARequest;
import org.example.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SourceSinkService {
    private static final Logger logger = LoggerFactory.getLogger(SourceSinkService.class);

    private final RestClient restClient;
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    public SourceSinkService(XmlMapper xmlMapper, RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.xmlMapper = xmlMapper;
        this.objectMapper = objectMapper;
    }

    public Optional<Response> getSourceAResponse() {
        return getResponse("/source/a", objectMapper);
    }

    public Optional<Response> getSourceBResponse() {
        return getResponse("/source/b", xmlMapper);
    }

    private Optional<Response> getResponse(String uri, ObjectMapper mapper) {
        logger.info("Making request");
        String body = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        logger.info("Response: " + body);

        if (mapper instanceof XmlMapper) {
            Pattern pattern = Pattern.compile("<msg>(<id value=\"(.*)\"\\/>|<(.*)\\/>)<\\/msg>");
            Matcher matcher = pattern.matcher(body);

            if (matcher.find()) {
                if (matcher.group(1).contains("done")) {
                    return Optional.of(new Response(matcher.group(3), null));
                }
                return Optional.of(new Response(null, matcher.group(2)));
            }
        }

        try {
            Response value = mapper.readValue(body, Response.class);
            return Optional.ofNullable(value);
        } catch (JsonProcessingException e) {
            logger.debug("Response is malformed: " + body);
            return Optional.empty();
        }
    }

    public void post(String kind, String id) {
        SinkARequest sinkAPayload = new SinkARequest(kind, id);
        try {
            String body = new ObjectMapper().writeValueAsString(sinkAPayload);
            int contentLength = body.getBytes().length;

            logger.info("Posting: " + body);

            restClient.post()
                    .uri("/sink/a")
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(contentLength)
                    .body(body)
                    .retrieve();

        } catch (JsonProcessingException e) {
            logger.error("Something went wrong: " + e);
            throw new RuntimeException("Something went wrong: " + e);
        }
    }
}

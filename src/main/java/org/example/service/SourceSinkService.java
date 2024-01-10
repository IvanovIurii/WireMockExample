package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.model.SinkARequest;
import org.example.model.SourceAResponse;
import org.example.model.SourceBResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class SourceSinkService {
    private static final Logger logger = LoggerFactory.getLogger(SolutionService.class);

    private final RestClient restClient;
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    public SourceSinkService(XmlMapper xmlMapper, RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.xmlMapper = xmlMapper;
        this.objectMapper = objectMapper;
    }

    public Optional<SourceAResponse> getSourceAResponse() {
        return getResponse("/source/a", objectMapper, SourceAResponse.class);
    }

    public Optional<SourceBResponse> getSourceBResponse() {
        return getResponse("/source/b", xmlMapper, SourceBResponse.class);
    }

    private <T> Optional<T> getResponse(String uri, ObjectMapper mapper, Class<T> cls) {
        logger.info("Making request");
        String body = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        logger.info("Response: " + body);

        try {
            T value = mapper.readValue(body, cls);
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

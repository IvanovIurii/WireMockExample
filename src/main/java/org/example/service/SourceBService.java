package org.example.service;

import org.example.model.Response;
import org.example.model.XmlResponse;
import org.example.rest.RestClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SourceBService implements ResourceService {

    private final RestClient restClient;

    public SourceBService(RestClient restClient) {
        this.restClient = restClient;
    }

    // each service should parse response by itself
    @Override
    public Optional<Response> getResource() {
        try {
            XmlResponse xmlResponse = restClient.getSourceBResponse();
            XmlResponse.Node id = xmlResponse.id();
            XmlResponse.Node done = xmlResponse.done();

            if (id == null && done == null) {
                return Optional.empty();
            }
            return Optional.of(new Response(
                    done != null ? "done" : null,
                    id != null ? id.value() : null)
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

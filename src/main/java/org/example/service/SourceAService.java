package org.example.service;

import org.example.model.Response;
import org.example.rest.RestClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SourceAService implements ResourceService {

    private final RestClient restClient;

    public SourceAService(RestClient restClient) {
        this.restClient = restClient;
    }

    // another approach instead of Optional would be to implement a new field isValid
    @Override
    public Optional<Response> getResource() {
        try {
            return Optional.of(restClient.getSourceAResponse());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

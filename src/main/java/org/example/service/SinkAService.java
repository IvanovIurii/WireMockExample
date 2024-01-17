package org.example.service;

import org.example.model.Request;
import org.example.rest.RestClient;
import org.springframework.stereotype.Service;

@Service
public class SinkAService implements SinkService {

    private final RestClient restClient;

    public SinkAService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void performAction(String id, String kind) {
        Request request = new Request(kind, id);
        restClient.postSinkARequest(request);
    }
}

package org.example.service;

import org.example.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SourceSinkService {
    private static final Logger logger = LoggerFactory.getLogger(SourceSinkService.class);
    private final static ExecutorService executorService = Executors.newFixedThreadPool(2);

    private final List<ResourceService> resourceServiceList;
    private final List<SinkService> sinkServiceList;

    public SourceSinkService(List<ResourceService> resourceServiceList, List<SinkService> sinkServiceList) {
        this.resourceServiceList = resourceServiceList;
        this.sinkServiceList = sinkServiceList;
    }

    public List<Optional<Response>> getSourceResponses() {
        List<Optional<Response>> responseList = new ArrayList<>();

        for (ResourceService resourceService : resourceServiceList) {
            logger.info("Making request");
            Optional<Response> optionalResponse = resourceService.getResource();
            responseList.add(optionalResponse);
        }
        return responseList;
    }

    public List<Optional<Response>> getSourceResponsesAsync() {
        List<CompletableFuture<Optional<Response>>> futures = resourceServiceList.stream()
                .map(resourceService -> CompletableFuture.supplyAsync(resourceService::getResource, executorService))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    // not very straightforward name IMHO
    public void performActions(String id, String kind) {
        for (SinkService sinkService : sinkServiceList) {
            logger.info("Posting: id=" + id + "; kind=" + kind);
            sinkService.performAction(id, kind);
        }
    }

}

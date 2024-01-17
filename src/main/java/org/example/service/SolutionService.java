package org.example.service;

import org.example.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class SolutionService {
    private static final Logger logger = LoggerFactory.getLogger(SolutionService.class);

    private final SourceSinkService sourceSinkService;
    private final Set<String> ids = new HashSet<>();

    public SolutionService(SourceSinkService sourceSinkService) {
        this.sourceSinkService = sourceSinkService;
    }

    public void execute() {
        logger.info("Start the process");
        while (true) {
            Optional<Response> optionalSourceA = sourceSinkService.getSourceAResponse();
            boolean isDoneA = false;

            if (optionalSourceA.isPresent()) {
                Response sourceAResponse = optionalSourceA.get();
                isDoneA = processSourceResponse(sourceAResponse);
            }

            Optional<Response> optionalSourceB = sourceSinkService.getSourceBResponse();
            boolean isDoneB = false;

            if (optionalSourceB.isPresent()) {
                Response sourceBResponse = optionalSourceB.get();
                isDoneB = processSourceResponse(sourceBResponse);
            }

            if (isDoneA && isDoneB) break;
        }
        postOrphaned();
        logger.info("End the process");
    }

    private boolean processSourceResponse(Response sourceResponse) {
        boolean isDone;
        isDone = "done".equals(sourceResponse.status());
        if (!isDone) {
            String id = sourceResponse.id();
            postJoined(id);
        }
        return isDone;
    }

    private void postJoined(String id) {
        if (ids.contains(id)) {
            sourceSinkService.post("joined", id);
            ids.remove(id);
        } else {
            ids.add(id);
        }
    }

    private void postOrphaned() {
        logger.info("Orphaned left, posting");
        if (!ids.isEmpty()) {
            ids.forEach(id -> sourceSinkService.post("orphaned", id));
        }
    }
}

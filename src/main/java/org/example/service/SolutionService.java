package org.example.service;

import org.example.model.SourceResponse;
import org.example.model.SourceAResponse;
import org.example.model.SourceBResponse;
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
    private Set<String> ids = new HashSet<>();

    public SolutionService(SourceSinkService sourceSinkService) {
        this.sourceSinkService = sourceSinkService;
    }

    public void execute() {
        logger.info("Start the process");
        while (true) {
            Optional<SourceAResponse> optionalSourceA = sourceSinkService.getSourceAResponse();
            boolean isDoneA = false;

            if (optionalSourceA.isPresent()) {
                SourceAResponse sourceAResponse = optionalSourceA.get();
                isDoneA = processSourceResponse(sourceAResponse);
            }

            Optional<SourceBResponse> optionalSourceB = sourceSinkService.getSourceBResponse();
            boolean isDoneB = false;

            if (optionalSourceB.isPresent()) {
                SourceBResponse sourceBResponse = optionalSourceB.get();
                isDoneB = processSourceResponse(sourceBResponse);
            }

            if (isDoneA && isDoneB) break;
        }
        postOrphaned();
        logger.info("End the process");
    }

    private boolean processSourceResponse(SourceResponse source) {
        boolean isDoneA;
        isDoneA = source.isDone();
        // id can be null when it is done
        if (!isDoneA) {
            String id = source.getId();
            postJoined(id);
        }
        return isDoneA;
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

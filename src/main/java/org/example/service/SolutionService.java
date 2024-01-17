package org.example.service;

import org.example.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SolutionService {
    private static final Logger logger = LoggerFactory.getLogger(SolutionService.class);
    private static final int SIZE_JOINED = 2; // size can be taken from sourceSinkService, it is an amount of sources

    private final SourceSinkService sourceSinkService;
    private final Map<String, Integer> idCountMap = new HashMap<>();

    public SolutionService(SourceSinkService sourceSinkService) {
        this.sourceSinkService = sourceSinkService;
    }

    public void execute() {
        logger.info("Start the process");
        while (true) {
            List<Optional<Response>> sourceResponses = sourceSinkService.getSourceResponses();
            boolean isDone = sourceResponses.stream()
                    .allMatch(response ->
                            response.isPresent() && "done".equals(response.get().status())
                    );
            if (isDone) break;

            sourceResponses.forEach(response -> response.ifPresent(value -> processSourceResponse(value.id())));
        }
        postOrphaned();
        logger.info("End the process");
    }

    private void processSourceResponse(String id) {
        if (id != null) {
            postJoined(id);
        }
    }

    private void postJoined(String id) {
        if (idCountMap.containsKey(id)) {
            idCountMap.put(id, idCountMap.get(id) + 1);
        } else {
            idCountMap.put(id, 0);
        }

        Integer amount = idCountMap.get(id);
        if (++amount == SIZE_JOINED) {
            sourceSinkService.performActions(id, "joined");
            idCountMap.remove(id);
        }
    }

    private void postOrphaned() {
        logger.info("Orphaned left, posting");
        if (!idCountMap.entrySet().isEmpty()) {
            idCountMap.forEach((key, value) -> sourceSinkService.performActions(key, "orphaned"));
        }
    }
}

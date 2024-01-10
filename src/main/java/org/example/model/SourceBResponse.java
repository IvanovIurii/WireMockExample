package org.example.model;

public record SourceBResponse(SourceBIdNode id, SourceBDoneNode done) implements SourceResponse {
    @Override
    public String getId() {
        return id.value();
    }

    public boolean isDone() {
        return done != null;
    }
}


package org.example.model;

public record SourceAResponse(String status, String id) implements SourceResponse {
    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isDone() {
        return "done".equals(status);
    }
}

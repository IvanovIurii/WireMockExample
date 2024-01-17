package org.example.model;

public record XmlResponse(Node id, Node done) {
    public record Node(String value) {
    }
}

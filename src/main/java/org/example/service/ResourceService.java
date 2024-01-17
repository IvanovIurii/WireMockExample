package org.example.service;

import org.example.model.Response;

import java.util.Optional;

public interface ResourceService {
    Optional<Response> getResource();
}

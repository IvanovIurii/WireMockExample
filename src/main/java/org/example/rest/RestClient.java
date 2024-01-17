package org.example.rest;

import feign.Headers;
import org.example.model.Request;
import org.example.model.Response;
import org.example.model.XmlResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "client", url = "${fixture.url}")
public interface RestClient {
    @GetMapping(value = "/source/a")
    Response getSourceAResponse();

    @GetMapping(value = "/source/b")
    XmlResponse getSourceBResponse();

    @PostMapping("/sink/a")
    @Headers("Content-Type: application/json")
    void postSinkARequest(@RequestBody Request request);
}

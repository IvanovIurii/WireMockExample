package org.example.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.example.TestConfiguration;
import org.example.model.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestConfiguration.class)
public class SourceSinkServiceTest {

    private WireMockServer wireMockServer;
    @Autowired
    private SourceSinkService sut;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(7299));
        wireMockServer.start();
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    public void shouldGetValidSourceAResponse() {
        String payload = "{\"status\":\"ok\",\"id\":\"123\"}";
        stubForSourceA(payload);

        Optional<Response> sourceAResponse = sut.getSourceAResponse();
        assertTrue(sourceAResponse.isPresent());
        assertEquals("123", sourceAResponse.get().id());
        assertEquals("ok", sourceAResponse.get().status());
    }

    @Test
    public void shouldGetSkipMalformedSourceAResponse() {
        String payload = "{\"status\":\"ok\",\"id\":\"123}";
        stubForSourceA(payload);

        Optional<Response> sourceAResponse = sut.getSourceAResponse();
        assertTrue(sourceAResponse.isEmpty());
    }

    @Test
    public void shouldReturnDoneStatusForSourceAResponse() {
        String payload = "{\"status\":\"done\"}";
        stubForSourceA(payload);

        Optional<Response> sourceAResponse = sut.getSourceAResponse();
        assertTrue(sourceAResponse.isPresent());
        assertEquals("done", sourceAResponse.get().status());
        assertNull(sourceAResponse.get().id());
    }

    @Test
    public void shouldGetValidSourceBResponse() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><id value=\"1378535db30df7a0f67a3fc7832ee8e5\"/></msg>";
        stubForSourceB(payload);

        Optional<Response> sourceAResponse = sut.getSourceBResponse();
        assertTrue(sourceAResponse.isPresent());
        assertEquals("1378535db30df7a0f67a3fc7832ee8e5", sourceAResponse.get().id());
        assertNull(sourceAResponse.get().status());
    }

    @Test
    public void shouldGetSkipMalformedSourceBResponse() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg>!@#$!@#$<foo/></msg>";
        stubForSourceB(payload);

        Optional<Response> sourceAResponse = sut.getSourceBResponse();
        assertTrue(sourceAResponse.isEmpty());
    }

    @Test
    public void shouldReturnDoneStatusForSourceBResponse() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><done/></msg>";
        stubForSourceB(payload);

        Optional<Response> sourceAResponse = sut.getSourceBResponse();
        assertTrue(sourceAResponse.isPresent());
        assertEquals("done", sourceAResponse.get().status());
    }

    @Test
    public void shouldPostWithoutAnyError() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/sink/a")));
        assertDoesNotThrow(() -> sut.post("joined", "1"));
    }


    private void stubForSourceA(String payload) {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/source/a"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(payload)));
    }

    private void stubForSourceB(String payload) {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/source/b"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/xml")
                        .withBody(payload)));
    }
}
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    public void shouldGetValidSourcesResponses() {
        String payloadA = "{\"status\":\"ok\",\"id\":\"123\"}";
        stubForSourceA(payloadA);

        String payloadB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><id value=\"1378535db30df7a0f67a3fc7832ee8e5\"/></msg>";
        stubForSourceB(payloadB);

        List<Optional<Response>> responseList = sut.getSourceResponses();
        assertEquals(2, responseList.size());

        assertEquals("123", responseList.get(0).get().id());
        assertEquals("ok", responseList.get(0).get().status());

        assertEquals("1378535db30df7a0f67a3fc7832ee8e5", responseList.get(1).get().id());
        assertNull(responseList.get(1).get().status());
    }

    @Test
    public void shouldGetValidSourcesResponsesWhenXMLMalformed() {
        String payloadA = "{\"status\":\"ok\",\"id\":\"123\"}";
        stubForSourceA(payloadA);

        String payloadB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg>!@#$!@#$<foo/></msg>";
        stubForSourceB(payloadB);

        List<Optional<Response>> responseList = sut.getSourceResponses();
        assertEquals(2, responseList.size());

        assertEquals("123", responseList.get(0).get().id());
        assertEquals("ok", responseList.get(0).get().status());

        assertFalse(responseList.get(1).isPresent());
    }

    @Test
    public void shouldGetValidSourcesResponsesWhenJSONMalformed() {
        String payloadA = "{\"status\":\"ok\",\"id\":\"123}";
        stubForSourceA(payloadA);

        String payloadB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><id value=\"1378535db30df7a0f67a3fc7832ee8e5\"/></msg>";
        stubForSourceB(payloadB);

        List<Optional<Response>> responseList = sut.getSourceResponses();
        assertEquals(2, responseList.size());

        assertFalse(responseList.get(0).isPresent());

        assertEquals("1378535db30df7a0f67a3fc7832ee8e5", responseList.get(1).get().id());
        assertNull(responseList.get(1).get().status());
    }

    @Test
    public void shouldGetEmptyListFromSourcesResponseWhenBothXMLAndJSONMalformed() {
        String payloadA = "{\"status\":\"ok\",\"id\":\"123}";
        stubForSourceA(payloadA);

        String payloadB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><ida asd f></msg>";
        stubForSourceB(payloadB);

        List<Optional<Response>> responseList = sut.getSourceResponses();
        assertEquals(2, responseList.size());

        assertFalse(responseList.get(0).isPresent());
        assertFalse(responseList.get(1).isPresent());
    }

    @Test
    public void shouldGetValidSourcesResponsesWhenXMLIsDone() {
        String payloadA = "{\"status\":\"ok\",\"id\":\"123\"}";
        stubForSourceA(payloadA);

        String payloadB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><done/></msg>";
        stubForSourceB(payloadB);

        List<Optional<Response>> responseList = sut.getSourceResponses();
        assertEquals(2, responseList.size());

        assertEquals("123", responseList.get(0).get().id());
        assertEquals("ok", responseList.get(0).get().status());

        assertNull(responseList.get(1).get().id());
        assertEquals("done", responseList.get(1).get().status());
    }

    @Test
    public void shouldGetValidSourcesResponsesWhenJSONIsDone() {
        String payloadA = "{\"status\":\"done\"}";
        stubForSourceA(payloadA);

        String payloadB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><id value=\"1378535db30df7a0f67a3fc7832ee8e5\"/></msg>";
        stubForSourceB(payloadB);

        List<Optional<Response>> responseList = sut.getSourceResponses();
        assertEquals(2, responseList.size());

        assertNull(responseList.get(0).get().id());
        assertEquals("done", responseList.get(0).get().status());

        assertEquals("1378535db30df7a0f67a3fc7832ee8e5", responseList.get(1).get().id());
        assertNull(responseList.get(1).get().status());
    }

    @Test
    public void shouldGetValidSourcesResponsesWhenBothJSONAndXMLDone() {
        String payloadA = "{\"status\":\"done\"}";
        stubForSourceA(payloadA);

        String payloadB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg><done/></msg>";
        stubForSourceB(payloadB);

        List<Optional<Response>> responseList = sut.getSourceResponses();
        assertEquals(2, responseList.size());

        assertNull(responseList.get(0).get().id());
        assertEquals("done", responseList.get(0).get().status());

        assertNull(responseList.get(1).get().id());
        assertEquals("done", responseList.get(1).get().status());
    }

    // however there is no test for content-length problem
    @Test
    public void shouldPostWithoutAnyError() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/sink/a")));
        assertDoesNotThrow(() -> sut.performActions("joined", "1"));
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
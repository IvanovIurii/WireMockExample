package org.example.service;

import org.example.model.SourceAResponse;
import org.example.model.SourceBDoneNode;
import org.example.model.SourceBIdNode;
import org.example.model.SourceBResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionServiceTest {

    @Mock
    private SourceSinkService sourceSinkServiceMock;
    @Captor
    private ArgumentCaptor<String> kindArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> idArgumentCaptor;
    private SolutionService sut;

    @BeforeEach
    public void setup() {
        sut = new SolutionService(sourceSinkServiceMock);
    }

    @Test
    void shouldExecuteCorrectly() {
        SourceAResponse sourceAResponseJoined1 = new SourceAResponse("ok", "1");
        SourceAResponse sourceAResponseJoined2 = new SourceAResponse("ok", "2");
        SourceAResponse sourceAResponseOrphaned = new SourceAResponse("ok", "123");
        SourceAResponse sourceAResponseDone = new SourceAResponse("done", null);

        Optional<SourceAResponse> sourceAResponseMalformedSkipped = Optional.empty();

        when(sourceSinkServiceMock.getSourceAResponse())
                .thenReturn(Optional.of(sourceAResponseJoined1))
                .thenReturn(Optional.of(sourceAResponseOrphaned))
                .thenReturn(sourceAResponseMalformedSkipped)
                .thenReturn(Optional.of(sourceAResponseJoined2))
                .thenReturn(Optional.of(sourceAResponseDone));

        SourceBResponse sourceBResponseJoined1 = new SourceBResponse(new SourceBIdNode("1"), null);
        SourceBResponse sourceBResponseJoined2 = new SourceBResponse(new SourceBIdNode("2"), null);
        SourceBResponse sourceBResponseOrphaned1 = new SourceBResponse(new SourceBIdNode("456"), null);
        SourceBResponse sourceBResponseOrphaned2 = new SourceBResponse(new SourceBIdNode("789"), null);
        SourceBResponse sourceBResponseOrphaned3 = new SourceBResponse(new SourceBIdNode("999"), null);
        SourceBResponse sourceBResponseDone = new SourceBResponse(null, new SourceBDoneNode());

        Optional<SourceBResponse> sourceBResponseMalformedSkipped = Optional.empty();

        when(sourceSinkServiceMock.getSourceBResponse())
                .thenReturn(Optional.of(sourceBResponseOrphaned1))
                .thenReturn(Optional.of(sourceBResponseOrphaned2))
                .thenReturn(sourceBResponseMalformedSkipped)
                .thenReturn(Optional.of(sourceBResponseOrphaned3))
                .thenReturn(Optional.of(sourceBResponseJoined1))
                .thenReturn(Optional.of(sourceBResponseJoined2))
                .thenReturn(Optional.of(sourceBResponseDone));

        sut.execute();

        verify(sourceSinkServiceMock, times(6)).post(kindArgumentCaptor.capture(), idArgumentCaptor.capture());

        List<String> firstArgs = kindArgumentCaptor.getAllValues();
        List<String> secondArgs = idArgumentCaptor.getAllValues();

        assertEquals("joined", firstArgs.get(0));
        assertEquals("joined", firstArgs.get(1));
        assertEquals("orphaned", firstArgs.get(2));
        assertEquals("orphaned", firstArgs.get(3));
        assertEquals("orphaned", firstArgs.get(4));
        assertEquals("orphaned", firstArgs.get(5));

        assertEquals("1", secondArgs.get(0));
        assertEquals("2", secondArgs.get(1));
        assertEquals("123", secondArgs.get(2));
        assertEquals("456", secondArgs.get(3));
        assertEquals("789", secondArgs.get(4));
        assertEquals("999", secondArgs.get(5));
    }
}
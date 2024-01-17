package org.example.service;

import org.example.model.Response;
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
        when(sourceSinkServiceMock.getSourceResponses())
                .thenReturn(List.of(
                        Optional.of(new Response("ok", "1")),
                        Optional.of(new Response(null, "999"))
                ))
                .thenReturn(List.of(
                        Optional.of(new Response("ok", "2")),
                        Optional.of(new Response(null, "2"))
                ))
                .thenReturn(List.of(
                        Optional.of(new Response(null, "1")),
                        Optional.empty()
                ))
                .thenReturn(List.of(
                        Optional.of(new Response("ok", "123")),
                        Optional.of(new Response(null, "456"))
                ))
                .thenReturn(List.of(
                        Optional.of(new Response("done", null)),
                        Optional.empty()
                ))
                .thenReturn(List.of(
                        Optional.of(new Response(null, "789")),
                        Optional.of(new Response("done", null))
                ))
                .thenReturn(List.of(
                        Optional.of(new Response("done", null)),
                        Optional.of(new Response("done", null))
                ));

        sut.execute();

        verify(sourceSinkServiceMock, times(6)).performActions(kindArgumentCaptor.capture(), idArgumentCaptor.capture());

        List<String> firstArgs = kindArgumentCaptor.getAllValues();
        List<String> secondArgs = idArgumentCaptor.getAllValues();

        assertEquals("joined", secondArgs.get(0));
        assertEquals("joined", secondArgs.get(1));
        assertEquals("orphaned", secondArgs.get(2));
        assertEquals("orphaned", secondArgs.get(3));
        assertEquals("orphaned", secondArgs.get(4));
        assertEquals("orphaned", secondArgs.get(5));

        assertEquals("2", firstArgs.get(0));
        assertEquals("1", firstArgs.get(1));
        assertEquals("123", firstArgs.get(2));
        assertEquals("456", firstArgs.get(3));
        assertEquals("789", firstArgs.get(4));
        assertEquals("999", firstArgs.get(5));
    }
}
package dev.jvmdocs.assistant;

import dev.jvmdocs.assistant.api.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private DocumentationService documentationService;

    @BeforeEach
    void setUp() {
        VirtualTimeScheduler.getOrSet();
    }

    @AfterEach
    void tearDown() {
        VirtualTimeScheduler.reset();
    }

    @InjectMocks
    private ChatController chatController;

    @Test
    void shouldReturnChatResponseSuccessfully() {
        // Given
        Question question = new Question("test query");
        String conversationId = "testConversation";
        Flux<String> expectedResponse = Flux.just("Hello", "World");

        when(documentationService.chat(any(Question.class), eq(conversationId)))
                .thenReturn(expectedResponse);

        // When
        Flux<String> result = chatController.chat(question, conversationId);

        // Then
        StepVerifier.create(result)
                .expectNext("Hello")
                .expectNext("World")
                .verifyComplete();

        verify(documentationService).chat(question, conversationId);
    }

    @Test
    void shouldUseDefaultConversationId() {
        // Given
        Question question = new Question("test query");
        Flux<String> expectedResponse = Flux.just("Response");

        when(documentationService.chat(any(Question.class), eq("defaultConversation")))
                .thenReturn(expectedResponse);

        // When
        Flux<String> result = chatController.chat(question, "defaultConversation");

        // Then
        StepVerifier.create(result)
                .expectNext("Response")
                .verifyComplete();

        verify(documentationService).chat(question, "defaultConversation");
    }

}

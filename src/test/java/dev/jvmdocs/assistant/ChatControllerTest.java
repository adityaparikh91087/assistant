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

    @Test
    void shouldRetryOnError() {
        // Given
        Question question = new Question("test query");
        String conversationId = "testConversation";

        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(invocation -> {
            switch (callCount.getAndIncrement()) {
                case 0:
                    return Flux.error(new RuntimeException("First attempt failed"));
                case 1:
                    return Flux.error(new RuntimeException("Second attempt failed"));
                default:
                    return Flux.just("Success after retry");
            }
        }).when(documentationService).chat(any(Question.class), eq(conversationId));

        // When
        Flux<String> result = chatController.chat(question, conversationId);

        // Then
        StepVerifier.withVirtualTime(() -> result)
                .thenAwait(Duration.ofMillis(100))
                .expectNext("Success after retry")
                .verifyComplete();

        // Verify that service was called 3 times (2 failures + 1 success)
        verify(documentationService, times(3)).chat(question, conversationId);
    }

    @Test
    void shouldFailAfterMaxRetries() {
        // Given
        Question question = new Question("test query");
        String conversationId = "testConversation";
        RuntimeException exception = new RuntimeException("Service unavailable");

        // Always return error to trigger all retries (1 initial + 3 retries = 4 total attempts)
        doAnswer(invocation -> Flux.error(exception))
                .when(documentationService)
                .chat(any(Question.class), eq(conversationId));

        // When
        Flux<String> result = chatController.chat(question, conversationId);

        // Then
        StepVerifier.withVirtualTime(() -> result)
                .thenAwait(Duration.ofMillis(100))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Service unavailable"))
                .verify();

        // Verify service was called 4 times (1 initial + 3 retries)
        verify(documentationService, times(4)).chat(question, conversationId);
    }
}

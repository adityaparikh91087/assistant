package dev.jvmdocs.assistant;

import dev.jvmdocs.assistant.api.Question;
import dev.jvmdocs.assistant.eol.EndOfLifeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = ChatController.class)
@Import({ChatControllerWebMvcTest.TestConfig.class})
class ChatControllerWebMvcTest {

    @Configuration
    static class TestConfig {
        @Bean
        public DocumentationService documentationService() {
            return mock(DocumentationService.class);
        }

        @Bean
        public EndOfLifeService endOfLifeService() {
            return mock(EndOfLifeService.class);
        }

        @Bean
        public ChatController chatController(DocumentationService documentationService) {
            return new ChatController(documentationService);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DocumentationService documentationService;

    @BeforeEach
    void setUp() {
        reset(documentationService);
    }

    @Test
    void shouldHandleChatEndpointSuccessfully() {
        // Given
        Question question = new Question("test query");
        String conversationId = "testConversation";
        Flux<String> response = Flux.just("Hello", "World");

        when(documentationService.chat(any(Question.class), eq(conversationId)))
                .thenReturn(response);

        // When & Then
        webTestClient.post()
                .uri("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X_CONV_ID", conversationId)
                .bodyValue(question)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Content-Type", "text/event-stream(;charset=.*)?")
                .expectBodyList(String.class)
                .hasSize(2)
                .contains("Hello", "World");

        verify(documentationService).chat(any(Question.class), eq(conversationId));
    }

    @Test
    void shouldHandleChatEndpointWithDefaultConversationId() {
        // Given
        Question question = new Question("test query");
        Flux<String> response = Flux.just("Response");

        when(documentationService.chat(any(Question.class), eq("defaultConversation")))
                .thenReturn(response);

        // When & Then
        webTestClient.post()
                .uri("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(question)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .contains("Response");

        verify(documentationService).chat(any(Question.class), eq("defaultConversation"));
    }

    @Test
    void shouldHandleEolEndpointSuccessfully() {
        // Given
        EndOfLifeService.Request eolRequest = new EndOfLifeService.Request("spring-boot");
        String conversationId = "testConversation";
        Flux<String> response = Flux.just("EOL Response");

        when(documentationService.eol(any(EndOfLifeService.Request.class), eq(conversationId)))
                .thenReturn(response);

        // When & Then
        webTestClient.post()
                .uri("/eol")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X_CONV_ID", conversationId)
                .bodyValue(eolRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .contains("EOL Response");

        verify(documentationService).eol(any(EndOfLifeService.Request.class), eq(conversationId));
    }

    @Test
    void shouldHandleErrorInChatEndpoint() {
        // Given
        Question question = new Question("test query");
        String conversationId = "testConversation";

        when(documentationService.chat(any(Question.class), eq(conversationId)))
                .thenReturn(Flux.error(new RuntimeException("Test error")));

        // When & Then
        webTestClient.post()
                .uri("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X_CONV_ID", conversationId)
                .bodyValue(question)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldHandleRetryInChatEndpoint() {
        // Given
        Question question = new Question("test query");
        String conversationId = "testConversation";
        AtomicInteger attemptCount = new AtomicInteger(0);

        // Create a Flux that will fail twice and succeed on the third attempt
        Flux<String> responseFlux = Flux.<String>create(sink -> {
            int attempt = attemptCount.getAndIncrement();
            System.out.println("[DEBUG_LOG] Processing attempt " + attempt);
            if (attempt < 2) {
                sink.error(new RuntimeException("Temporary error"));
            } else {
                sink.next("Success after retry");
                sink.complete();
            }
        });

        when(documentationService.chat(any(Question.class), eq(conversationId)))
                .thenReturn(responseFlux);

        // When & Then
        webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(10))
                .build()
                .post()
                .uri("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X_CONV_ID", conversationId)
                .bodyValue(question)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Content-Type", "text/event-stream(;charset=.*)?")
                .expectBodyList(String.class)
                .hasSize(1)
                .contains("Success after retry");

        // Verify that the service was called exactly once (the Flux handles retries internally)
        verify(documentationService).chat(any(Question.class), eq(conversationId));
    }
}

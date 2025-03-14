package dev.jvmdocs.assistant;

import dev.jvmdocs.assistant.api.Question;
import dev.jvmdocs.assistant.eol.EndOfLifeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * The ChatController class serves as a REST API controller for handling requests related to chat functionality.
 * It provides an endpoint for querying and retrieving responses based on user input,
 * leveraging the application's DocumentationService for processing and generating responses.
 */
@RestController
public class ChatController {

    private final DocumentationService documentationService;

    public ChatController(DocumentationService documentationService) {
        this.documentationService = documentationService;
    }

    @PostMapping(path = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody Question question,
                             @RequestHeader(name="X_CONV_ID", defaultValue="defaultConversation") String conversationId) {
            return documentationService.chat(question, conversationId)
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                            .maxBackoff(Duration.ofSeconds(1)));
    }

    @PostMapping(path = "/eol", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> eol(@RequestBody EndOfLifeService.Request eolRequest,
                             @RequestHeader(name="X_CONV_ID", defaultValue="defaultConversation") String conversationId) {
        return documentationService.eol(eolRequest, conversationId)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .maxBackoff(Duration.ofSeconds(1)));
    }
}

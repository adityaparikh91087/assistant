package dev.jvmdocs.assistant;

import dev.jvmdocs.assistant.api.Question;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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

    @GetMapping("/")
    public Flux<String> chat(@RequestBody Question question,
                             @RequestHeader(name="X_CONV_ID", defaultValue="defaultConversation") String conversationId) {
            return documentationService.chat(question, conversationId)
                    .retry(3);
    }
}

package dev.jvmdocs.assistant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public Flux<String> chat(@RequestParam(defaultValue = "What is spring boot?") String query) {
        return documentationService.chat(query).retry(3);
    }
}

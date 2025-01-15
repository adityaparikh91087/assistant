package dev.jvmdocs.assistant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final DocumentationService documentationService;

    public ChatController(DocumentationService documentationService) {
        this.documentationService = documentationService;
    }

    @GetMapping("/")
    public Flux<String> chat(@RequestParam(defaultValue = "What is spring boot?") String query) {
        return documentationService.chat(query);
    }
}

package dev.jvmdocs.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    @Value("classpath:/prompts/spring-boot-reference.st")
    private Resource sbPromptTemplate;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                )
                .defaultFunctions("endOfLifeFunction")
                .build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/")
    public Flux<String> chat(@RequestParam(defaultValue = "What is spring boot?") String query) {
        var similarDocuments = findSimilarDocuments(query);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(sbPromptTemplate);
        var systemMessage = systemPromptTemplate.createMessage(
                Map.of("documents", similarDocuments));

        var userMessage = new UserMessage(query);
        var prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    private String findSimilarDocuments(String message) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(3));
        return similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}

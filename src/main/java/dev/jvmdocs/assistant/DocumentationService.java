package dev.jvmdocs.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * The DocumentationService class is responsible for providing chat-based interactions
 * using a pre-defined prompt template and a collection of documents. It integrates a
 * chat client and a vector store to handle queries and retrieve relevant information.
 *
 * This service enables users to input queries, processes the input to identify
 * related documents using similarity search, and generates responses using a chat client.
 */
@Service
public class DocumentationService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Resource sbPromptTemplate;

    public DocumentationService(ChatClient.Builder builder,
                                VectorStore vectorStore,
                                @Value("classpath:/prompts/spring-boot-reference.st") Resource sbPromptTemplate) {
        this.sbPromptTemplate = sbPromptTemplate;
        this.vectorStore = vectorStore;
        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new SimpleLoggerAdvisor()
                )
                .defaultFunctions("endOfLifeFunction")
                .build();
    }

    public Flux<String> chat(String query) {
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
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(message)
                        .withTopK(3));
        return similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}

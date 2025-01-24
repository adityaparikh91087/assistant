package dev.jvmdocs.assistant.shell;

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
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.shell.command.annotation.Command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SpringAssistantCommand is a shell command component that integrates with a chat client
 * and vector-based search capabilities to provide intelligent query responses.
 *
 * This class leverages the functionality of a {@link ChatClient} and a {@link VectorStore}
 * to process user-provided questions, find similar documents, and construct responses
 * using a system prompt template.
 *
 * The command is registered under the alias "q" and handles queries to provide relevant
 * information based on the context and similarity search from a predefined knowledge base.
 *
 * Key functionality:
 * - Executes a similarity search for documents based on user queries using {@link VectorStore}.
 * - Constructs system messages using a prompt template provided as a resource.
 * - Interacts with the {@link ChatClient} to generate responses.
 */
@Command
public class SpringAssistantCommand {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    @Value("classpath:/prompts/spring-boot-reference.st")
    private Resource sbPromptTemplate;

    public SpringAssistantCommand(ChatClient.Builder builder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                )
                .defaultFunctions("endOfLifeFunction")
                .build();
    }

    @Command(command = "q")
    public String question(@DefaultValue(value = "What is Spring Boot") String question) {
        var similarDocuments = findSimilarDocuments(question);

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(sbPromptTemplate);
        var systemMessage = systemPromptTemplate.createMessage(
                Map.of("documents", similarDocuments));

        var userMessage = new UserMessage(question);
        var prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.prompt(prompt)
                .call()
                .content();

    }

    private String findSimilarDocuments(String message) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(3));
        return similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}

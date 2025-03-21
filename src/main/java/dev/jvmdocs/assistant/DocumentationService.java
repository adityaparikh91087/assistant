package dev.jvmdocs.assistant;

import dev.jvmdocs.assistant.api.Answer;
import dev.jvmdocs.assistant.api.Question;
import dev.jvmdocs.assistant.eol.EndOfLifeService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


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
    private final Resource systemPromptTemplate;
    private final EndOfLifeService endOfLifeService;

    public DocumentationService(ChatClient.Builder builder,
                                VectorStore vectorStore,
                                ChatMemory chatMemory,
                                @Value("classpath:/prompts/system_prompt.st") Resource systemPromptTemplate,
                                EndOfLifeService endOfLifeService) {
        this.systemPromptTemplate = systemPromptTemplate;
        this.vectorStore = vectorStore;
        this.endOfLifeService = endOfLifeService;
        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory), // CHAT MEMORY
                        new QuestionAnswerAdvisor(vectorStore), // RAG
                        new SimpleLoggerAdvisor())
                .build();
    }

    public Flux<String> chat(Question question, String chatId) {
        var prompt = getPrompt(question);
        return chatClient.prompt(prompt)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream()
                .content();
    }

    public Flux<String> eol(EndOfLifeService.Request eolRequest, String chatId) {
        var prompt = getPrompt(eolRequest);
        return chatClient.prompt(prompt)
                .tools(endOfLifeService)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream()
                .content();
    }

    public Answer ask(Question question, String chatId) {
        var prompt = getPrompt(question);
        return chatClient.prompt(prompt)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call()
                .entity(Answer.class);
    }

    public EndOfLifeService.Response eolResponse(EndOfLifeService.Request eolRequest, String chatId) {
        var prompt = getPrompt(eolRequest);
        return chatClient.prompt(prompt)
                .tools(endOfLifeService)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call()
                .entity(EndOfLifeService.Response.class);
    }

    private Prompt getPrompt(EndOfLifeService.Request eolRequest) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptTemplate);
        var systemMessage = systemPromptTemplate.createMessage();
        var userMessage = new UserMessage(eolRequest.product());
        return new Prompt(List.of(systemMessage, userMessage));
    }

    private Prompt getPrompt(Question question) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptTemplate);
        var systemMessage = systemPromptTemplate.createMessage();
        var userMessage = new UserMessage(question.query());
        return new Prompt(List.of(systemMessage, userMessage));
    }

}

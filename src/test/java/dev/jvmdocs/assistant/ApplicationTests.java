package dev.jvmdocs.assistant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

    @Autowired
    DocumentationService documentationService;
    @Autowired
    private ChatClient.Builder chatClientBuilder;
    private RelevancyEvaluator relevancyEvaluator;

    @BeforeEach
    void setup() {
        relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void evaluateRelevancy() {
        String query = "How to write a GraphQL Service?";
        String answer = documentationService.chat(query)
                .collectList()
                .map(strings -> String.join("", strings))
                .block();

        EvaluationRequest evaluationRequest = new EvaluationRequest(query, answer);
        EvaluationResponse response = relevancyEvaluator.evaluate(evaluationRequest);

        assertThat(response.isPass())
                .withFailMessage("""
                                ========================================
                                The answer "%s"
                                is not considered relevant to the question/**/
                                "%s".
                                ========================================
                                """,
                        answer,
                        query)
                .isTrue();
    }

}

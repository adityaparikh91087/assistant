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
class RelevancyEvaluatorTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;
    private RelevancyEvaluator relevancyEvaluator;

    @BeforeEach
    void setup() {
        relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
    }

    @Test
    void testRelevancyEvaluation() {
        // Test messages
        String answer1 = "Java is a popular programming language used for building enterprise applications.";
        String answer2 = "Python is a versatile scripting language often used in data science.";
        String query = "Tell me about Java programming";

        // Create evaluation requests
        EvaluationRequest request1 = new EvaluationRequest(query, answer1);
        EvaluationRequest request2 = new EvaluationRequest(query, answer2);

        // Evaluate relevancy
        EvaluationResponse response1 = relevancyEvaluator.evaluate(request1);
        EvaluationResponse response2 = relevancyEvaluator.evaluate(request2);

        // Assert that Java-related answer is more relevant than Python-related answer
        assertThat(response1.getScore())
            .isGreaterThan(response2.getScore())
            .withFailMessage("Java answer (score: %f) should be more relevant than Python answer (score: %f) for Java query",
                response1.getScore(), response2.getScore());
    }
}

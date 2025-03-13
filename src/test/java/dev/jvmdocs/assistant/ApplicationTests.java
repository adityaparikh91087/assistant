package dev.jvmdocs.assistant;

import dev.jvmdocs.assistant.api.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

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

}

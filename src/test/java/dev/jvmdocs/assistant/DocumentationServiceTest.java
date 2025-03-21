package dev.jvmdocs.assistant;

import dev.jvmdocs.assistant.api.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DocumentationServiceTest {

    @Autowired
    private DocumentationService documentationService;

    @Test
    void shouldAnswerQuestion() {
        String query = "What is Apache Solr?";
        String answer = documentationService.ask(new Question(query), UUID.randomUUID().toString()).response();

        assertThat(answer)
            .isNotEmpty()
            .withFailMessage("Answer should not be empty");
    }

    @Test
    void shouldStreamAnswer() {
        String query = "What is Apache Solr?";

        StepVerifier.create(documentationService.chat(new Question(query), UUID.randomUUID().toString()))
            .thenConsumeWhile(message -> {
                assertThat(message).isNotEmpty();
                return true;
            })
            .verifyComplete();
    }
}

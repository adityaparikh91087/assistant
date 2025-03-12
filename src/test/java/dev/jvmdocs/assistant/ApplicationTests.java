package dev.jvmdocs.assistant;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import java.time.Duration;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

    private WireMockServer wireMockServer;

    @Autowired
    DocumentationService documentationService;
    @Autowired
    private ChatClient.Builder chatClientBuilder;
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private RestClient.Builder restClientBuilder;
    private RelevancyEvaluator relevancyEvaluator;

    @BeforeEach
    void setup() {
        relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testWebClientTimeout() {
        // Configure delayed response
        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(31000))); // Delay longer than our 30s timeout

        WebClient webClient = webClientBuilder
                .baseUrl(wireMockServer.baseUrl())
                .build();

        StepVerifier.create(
                webClient.get()
                        .uri("/test")
                        .retrieve()
                        .bodyToMono(String.class)
        )
        .expectError(WebClientResponseException.class)
        .verify(Duration.ofSeconds(35)); // Allow some buffer for the verification
    }

    @Test
    void testRestClientTimeout() {
        // Configure delayed response
        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(31000))); // Delay longer than our 30s timeout

        RestClient restClient = restClientBuilder
                .baseUrl(wireMockServer.baseUrl())
                .build();

        assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> restClient.get()
                    .uri("/test")
                    .retrieve()
                    .body(String.class)
            )
        ).isNotNull();
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

package dev.jvmdocs.assistant.ingestion

import org.junit.jupiter.api.Test
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.chromadb.ChromaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.core.io.ClassPathResource
import org.assertj.core.api.Assertions.assertThat
import org.springframework.ai.document.Document
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.vectorstore.SearchRequest
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

@SpringBootTest
@Testcontainers
class IngestionServiceTest {

    companion object {
        @JvmStatic
        @Container
        private val chromaDb = ChromaDBContainer("chromadb/chroma:latest")
            .withExposedPorts(8000)
            .withEnv("CHROMA_SERVER_HOST", "0.0.0.0")
            .withEnv("CHROMA_SERVER_HTTP_PORT", "8000")
            .waitingFor(Wait.forHttp("/api/v1/heartbeat")
                .forPort(8000)
                .withStartupTimeout(Duration.ofMinutes(2)))

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.ai.vectorstore.chroma.host") { chromaDb.host }
            registry.add("spring.ai.vectorstore.chroma.port") { chromaDb.firstMappedPort }
            registry.add("spring.ai.vectorstore.chroma.collection-name") { "test-collection" }
        }
    }

    @Autowired
    private lateinit var ingestionService: IngestionService

    @Autowired
    private lateinit var vectorStore: VectorStore

    @Test
    fun `should ingest PDF document into vector store`() {
        // Given
        val testPdf = ClassPathResource("/docs/Solr_in_Action.pdf")
        val config = PdfDocumentReaderConfig.builder()
            .withPageExtractedTextFormatter(
                ExtractedTextFormatter.Builder()
                    .withNumberOfBottomTextLinesToDelete(0)
                    .withNumberOfTopPagesToSkipBeforeDelete(0)
                    .build()
            )
            .withPagesPerDocument(1)
            .build()

        // When
        ingestionService.addToVectorStore(testPdf, config)

        // Then
        val results = vectorStore.similaritySearch("What is Solr?")

        assertThat(results).isNotNull
        assertThat(results).isNotEmpty
        assertThat(results?.get(0)?.text).containsIgnoringCase("Solr")
    }
}

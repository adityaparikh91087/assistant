package dev.jvmdocs.assistant.ingestion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.core.io.ClassPathResource
import org.testcontainers.chromadb.ChromaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class IngestionServiceTest {

    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        private val chromaDb = ChromaDBContainer("chromadb/chroma:latest");
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

package dev.jvmdocs.assistant;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service for ingesting documents into a vector store for further processing or search capabilities.
 * This service is responsible for loading document resources, processing them, and storing their
 * extracted contents into a {@link VectorStore} format.
 *
 * It supports PDF document ingestion with configurable processing settings, including formatting
 * and page-specific extraction parameters.
 *
 * The service initializes by loading predefined documents during application startup
 * and provides a method for adding documents programmatically.
 */
// do this only once
// @Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private final VectorStore vectorStore;
    @Value("classpath:/docs/spring-boot-reference.pdf")
    private Resource springBootReference;
    @Value("classpath:/docs/gradle_userguide.pdf")
    private Resource gradleUserGuide;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {

        log.info("Loading Spring Boot Reference PDF into Vector Store");
        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();

        addToVectorStore(springBootReference, config);
        log.info("Application is ready");
    }

    @Retryable(retryFor = Exception.class,
            maxAttempts = 10,
            backoff = @Backoff(multiplier = 1.5, random = true))
    public void addToVectorStore(Resource pdfResource,
                                 PdfDocumentReaderConfig config) {
        log.info("Starting");
        var pdfReader = new PagePdfDocumentReader(pdfResource, config);
        var textSplitter = new TokenTextSplitter();
        List<Document> documents = textSplitter.apply(pdfReader.get());
        vectorStore.add(documents);
        log.info("Done");
    }
}

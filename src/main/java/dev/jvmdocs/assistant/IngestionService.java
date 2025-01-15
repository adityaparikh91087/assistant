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

// do this only once
//@Service
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
    public void init() {

        log.info("Loading Spring Boot Reference PDF into Vector Store");
        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();

        addToVectorStore(springBootReference, config);
        addToVectorStore(gradleUserGuide, config);
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

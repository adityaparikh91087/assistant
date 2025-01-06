package dev.danvega.sbdocs;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Component
public class ReferenceDocsLoader {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDocsLoader.class);
    private final VectorStore vectorStore;
    @Value("classpath:/docs/spring-boot-reference.pdf")
    private Resource pdfResource;
    @Value("classpath:/docs/gradle_userguide.pdf")
    private Resource gradleUserGuide;

    public ReferenceDocsLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() {

        log.info("Loading Spring Boot Reference PDF into Vector Store");
        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();

        addToVectorStore(pdfResource, config);
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

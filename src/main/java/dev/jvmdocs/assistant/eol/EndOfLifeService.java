package dev.jvmdocs.assistant.eol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON;


/**
 * A service for interacting with the End of Life API that provides lifecycle data for products.
 * It uses Spring's RestClient to fetch data based on a product request and maps the response to Java objects.
 */
@Service
public class EndOfLifeService {

    private static final Logger log = LoggerFactory.getLogger(EndOfLifeService.class);

    private final RestClient restClient;

    public EndOfLifeService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Processes a product lifecycle data request by calling the End of Life API.
     *
     * @param request the {@link Request} object containing the product name
     * @return a {@link Response} containing the deserialized lifecycle data for the product
     */
    @Tool(description = "get eol for product")
    public Response eol(Request request) {
        log.info("End of Life API Request: {}", request);
        ParameterizedTypeReference<List<EolInfo>> typeReference = new ParameterizedTypeReference<>() {};
        List<EolInfo> eolInfos = restClient.get()
                .uri("https://endoflife.date/api/" + request.product().replaceAll("\\s", "-") + ".json")
                .accept(APPLICATION_JSON)
                .retrieve()
                .body(typeReference);
        Response response = new Response(eolInfos);
        log.info("End of Life API Response: {}", response);
        return response;
    }

    /**
     * Represents a request to fetch lifecycle data for a specific product from the API.
     *
     * @param product the name of the product for which lifecycle data is requested
     */
    public record Request(String product) {
    }

    public record Response(List<EolInfo> eolInfos) {
    }

    public record EolInfo(String cycle,
                          String release,
                          LocalDate releaseDate,
                          String eol,
                          String latest,
                          LocalDate latestReleaseDate,
                          boolean lts,
                          String support,
                          String discontinued) { }
}

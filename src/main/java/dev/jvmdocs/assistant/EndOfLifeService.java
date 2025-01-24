package dev.jvmdocs.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON;


/**
 * A service for interacting with the End of Life API that provides lifecycle data for products.
 * It uses Spring's RestClient to fetch data based on a product request and maps the response to Java objects.
 */
public class EndOfLifeService implements Function<EndOfLifeService.Request, EndOfLifeService.Response> {

    private static final Logger log = LoggerFactory.getLogger(EndOfLifeService.class);

    private final RestClient restClient;

    public EndOfLifeService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://endoflife.date/api")
                .build();
    }

    /**
     * Processes a product lifecycle data request by calling the End of Life API.
     *
     * @param request the {@link Request} object containing the product name
     * @return a {@link Response} containing the deserialized lifecycle data for the product
     */
    @Override
    public Response apply(Request request) {
        log.info("End of Life API Request: {}", request);
        ParameterizedTypeReference<List<Cycle>> typeReference = new ParameterizedTypeReference<>() {};
        List<Cycle> cycles = restClient.get()
                .uri("/{product}.json", request.product().replaceAll("\\s", "-"))
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(typeReference)
                .getBody();
        Response response = new Response(cycles);
        log.info("End of Life API Response: {}", response);
        return response;
    }

    /**
     * Represents a request to fetch lifecycle data for a specific product from the API.
     * 
     * @param product the name of the product for which lifecycle data is requested
     */
    public record Request(String product) {}
    public record Response(List<Cycle> cycles){}
    /**
     * Represents a product's lifecycle details as provided by the End of Life API.
     *
     * @param cycle the specific lifecycle stage of the product (e.g., version or iteration)
     * @param releaseDate the release date of the product cycle
     * @param eol the end-of-life date for the given product cycle
     * @param latest indicates if this is the latest release for this product cycle
     * @param link a URL link associated with additional information about this product cycle
     * @param lts specifies if the product cycle is a long-term support version
     * @param support text indicating the support status or duration for this cycle
     * @param discontinued text indicating if this product cycle is discontinued
     */
    public record Cycle(String cycle, String releaseDate, String eol, String latest, String link, boolean lts, String support, String discontinued) {}


}

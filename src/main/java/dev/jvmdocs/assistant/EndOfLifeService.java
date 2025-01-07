package dev.jvmdocs.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON;


public class EndOfLifeService implements Function<EndOfLifeService.Request, EndOfLifeService.Response> {

    private static final Logger log = LoggerFactory.getLogger(EndOfLifeService.class);

    private final RestClient restClient;

    public EndOfLifeService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://endoflife.date/api")
                .build();
    }

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

    public record Request(String product) {}
    public record Response(List<Cycle> cycles){}
    public record Cycle(String cycle, String releaseDate, String eol, String latest, String link, boolean lts, String support, String discontinued) {}


}

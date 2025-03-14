package dev.jvmdocs.assistant.eol;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Configuration
public class FunctionConfiguration {

    @Bean
    @Description("Get end of life (eol) information for given product")
    public Function<EndOfLifeService.Request, EndOfLifeService.Response> endOfLifeFunction(RestClient restClient) {
        return new EndOfLifeService(restClient);
    }
}

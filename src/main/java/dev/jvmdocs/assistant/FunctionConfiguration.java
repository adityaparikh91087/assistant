package dev.jvmdocs.assistant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Configuration
public class FunctionConfiguration {

    @Bean
    public RestClient restClient(RestClient.Builder builder, ClientLoggerRequestInterceptor requestInterceptor) {
        return builder
                .requestInterceptor(requestInterceptor)
                .build();
    }

    @Bean
    @Description("Get end of life (eol) information for given product")
    public Function<EndOfLifeService.Request, EndOfLifeService.Response> endOfLifeFunction(RestClient restClient) {
        return new EndOfLifeService(restClient);
    }
}

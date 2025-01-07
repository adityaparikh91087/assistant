package dev.jvmdocs.assistant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class FunctionConfiguration {

    @Bean
    @Description("Get the current end of life dates for given product.")
    public Function<EndOfLifeService.Request, EndOfLifeService.Response> endOfLifeFunction() {
        return new EndOfLifeService();
    }
}

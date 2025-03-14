package dev.jvmdocs.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;

import java.time.Duration;


@SpringBootApplication
@EnableRetry
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Bean
	public RestClientCustomizer restClientCustomizer() {
		return restClientBuilder -> {
 			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setReadTimeout(Duration.ofSeconds(30));
			factory.setConnectTimeout(Duration.ofSeconds(30));
			restClientBuilder.requestFactory(factory);
		};
	}

}

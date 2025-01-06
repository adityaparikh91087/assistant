package dev.danvega.sbdocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.shell.command.annotation.CommandScan;

@ImportRuntimeHints(HintsRegistrar.class)
@CommandScan
@SpringBootApplication
@EnableRetry
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

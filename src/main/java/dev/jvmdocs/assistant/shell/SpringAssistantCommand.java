package dev.jvmdocs.assistant.shell;

import dev.jvmdocs.assistant.DocumentationService;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.shell.command.annotation.Command;


@Command
public class SpringAssistantCommand {

    private final DocumentationService documentationService;

    public SpringAssistantCommand(DocumentationService documentationService) {
        this.documentationService = documentationService;
    }

    @Command(command = "q")
    public String question(@DefaultValue(value = "What is Spring Boot") String question) {
        return documentationService.ask(question);

    }

}

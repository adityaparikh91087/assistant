package dev.jvmdocs.assistant.shell;

import dev.jvmdocs.assistant.DocumentationService;
import dev.jvmdocs.assistant.api.Answer;
import dev.jvmdocs.assistant.api.Question;
import dev.jvmdocs.assistant.eol.EndOfLifeService;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.shell.command.annotation.Command;

import java.util.UUID;


@Command
public class SpringAssistantCommand {

    private final DocumentationService documentationService;

    public SpringAssistantCommand(DocumentationService documentationService) {
        this.documentationService = documentationService;
    }

    @Command(command = "q")
    public Answer question(@DefaultValue(value = "What is Spring Boot") String question) {
        return documentationService.ask(new Question(question), UUID.randomUUID().toString());

    }

    @Command(command = "eol")
    public EndOfLifeService.Response eol(@DefaultValue(value = "End of Life for Solr") String question) {
        return documentationService.eolResponse(new EndOfLifeService.Request(question), UUID.randomUUID().toString());

    }

}

package dev.jvmdocs.assistant.shell;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.shell.command.annotation.CommandScan;

@Configuration
@ImportRuntimeHints(HintsRegistrar.class)
@CommandScan
public class ShellConfig {
}

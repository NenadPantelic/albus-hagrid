package rs.ac.kg.fin.albus.hagrid.environment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CommandParser {

    private final EnvironmentParametersHandler environmentParametersHandler;

    public CommandParser(EnvironmentParametersHandler environmentParametersHandler) {
        this.environmentParametersHandler = environmentParametersHandler;
    }

    public String parseCommand(String environment, List<String> commandArguments) {
        log.info("Parsing a command for environment {}", environment);

        String commandTemplate = environmentParametersHandler.getCommandTemplate(environment);
        List<String> executionVariableArgs = environmentParametersHandler.getExecutionVariableArgs(environment);

        if (commandTemplate == null) {
            throw new HagridException(String.format("Unrecognized environment %s", environment));
        }

        int numOfArgs = executionVariableArgs.size();
        if (commandArguments.size() != numOfArgs) {
            throw new HagridException(
                    String.format("Command argument mismatch: expected %s, got %s", numOfArgs, commandArguments.size())
            );
        }

        for (int i = 0; i < numOfArgs; i++) {
            commandTemplate = commandTemplate.replaceAll(
                    escapeTemplateVariable(executionVariableArgs.get(i)),
                    commandArguments.get(i)
            );
        }

        return commandTemplate;
    }

    private String escapeTemplateVariable(String variable) {
        return Pattern.quote(
                String.format("@@{{%s}}@@", variable)
        );
    }
}

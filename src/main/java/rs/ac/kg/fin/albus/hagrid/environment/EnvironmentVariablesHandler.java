package rs.ac.kg.fin.albus.hagrid.environment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EnvironmentVariablesHandler {

    private static final Pattern RULE_PATTERN = Pattern.compile("__RULE<<(.*?)>>__");

    private static final Pattern UUID_GENERATOR_FUNCTION_PATTERN = Pattern.compile("UUID\\(\\)");
    private static final Pattern RANDOM_GENERATOR_FUNCTION_PATTERN = Pattern.compile("RANDOM\\((.*?)\\)");
    private static final Pattern CONST_GENERATOR_FUNCTION = Pattern.compile("CONST\\((.*?)\\)");

    private final EnvironmentParametersHandler environmentParametersHandler;

    public EnvironmentVariablesHandler(EnvironmentParametersHandler environmentParametersHandler) {
        this.environmentParametersHandler = environmentParametersHandler;
    }

    public Map<String, String> generateEnvironmentVariables(String environment) {
        log.info("Generate environment variables[environment = {}]", environment);

        Map<String, String> environmentVariableRules = environmentParametersHandler.getEnvironmentVariableRules(environment);

        return environmentVariableRules
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> generateEnvironmentVariableValue(entry.getValue())
                        )
                );
    }

    private String generateEnvironmentVariableValue(String ruleDefinition) {
        String generatorFunctionPattern = extractGeneratorFunctionFromRule(ruleDefinition);
        if (generatorFunctionPattern == null) {
            throw new HagridException("Invalid generator function");
        }

        return getValueFromGeneratorFunction(generatorFunctionPattern);
    }

    private String extractGeneratorFunctionFromRule(String ruleDefinition) {
        Matcher matcher = RULE_PATTERN.matcher(ruleDefinition);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String getValueFromGeneratorFunction(String generatorFunctionPattern) {
        if (UUID_GENERATOR_FUNCTION_PATTERN.matcher(generatorFunctionPattern).find()) {
            return UUID.randomUUID().toString();
        }

        Matcher randomGeneratorFunctionMatcher = RANDOM_GENERATOR_FUNCTION_PATTERN.matcher(generatorFunctionPattern);
        if (randomGeneratorFunctionMatcher.find()) {
            return random(Integer.parseInt(randomGeneratorFunctionMatcher.group(1)));
        }

        Matcher constGeneratorFunctionMatcher = CONST_GENERATOR_FUNCTION.matcher(generatorFunctionPattern);
        if (constGeneratorFunctionMatcher.find()) {
            return constGeneratorFunctionMatcher.group(1);
        }

        throw new HagridException(String.format("Invalid generator function %s", generatorFunctionPattern));
    }

    //// generator functions ////
    private String uuid() {
        return UUID.randomUUID().toString();
    }

    private String random(int limit) {
        return RandomStringUtils.random(limit);
    }
}

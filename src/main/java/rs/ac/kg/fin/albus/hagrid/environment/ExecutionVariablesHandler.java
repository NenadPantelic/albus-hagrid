package rs.ac.kg.fin.albus.hagrid.environment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rs.ac.kg.fin.albus.hagrid.data.environment.EnvironmentParameters;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExecutionVariablesHandler {

    private static final String ENVIRONMENT_VARIABLES_PREFIX = "environmentVariables.";

    private final EnvironmentParametersHandler environmentParametersHandler;

    public ExecutionVariablesHandler(EnvironmentParametersHandler environmentParametersHandler) {
        this.environmentParametersHandler = environmentParametersHandler;
    }

    public List<String> getExecutionVariables(String environment,
                                              Map<String, String> environmentVariables) {
        log.info("Get execution variables[environment = {}]....", environment);

        List<String> executionVariables = new ArrayList<>();
        EnvironmentParameters environmentParameters = environmentParametersHandler.get(environment);
        List<String> executionVariableArgs = environmentParameters.executionVariableArgs();
        Map<String, String> executionVariableRules = environmentParameters.executionVariableRules();

        for (String executionVariableArg : executionVariableArgs) {
            String executionVariableRule = executionVariableRules.get(executionVariableArg);
            if (executionVariableRule == null) {
                throw new HagridException(String.format("Execution variable %s has no rule set", executionVariableArg));
            }

            if (executionVariableRule.startsWith(ENVIRONMENT_VARIABLES_PREFIX)) {
                String environmentVariable = executionVariableRule.substring(ENVIRONMENT_VARIABLES_PREFIX.length());
                String environmentVariableValue = environmentVariables.get(environmentVariable);
                if (environmentVariableValue == null) {
                    throw new HagridException(
                            String.format("Environment variable %s is not set which is needed for the execution variable" +
                                    " %s", environmentVariable, executionVariableArg)
                    );
                }
                executionVariables.add(environmentVariableValue);
            } else {
                // TODO: should use the same rules
                executionVariables.add(executionVariableRule);
            }
        }

        return executionVariables;
    }
}

package rs.ac.kg.fin.albus.hagrid.environment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import rs.ac.kg.fin.albus.hagrid.config.EnvironmentParametersConfig;
import rs.ac.kg.fin.albus.hagrid.data.environment.EnvironmentParameters;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;
import rs.ac.kg.fin.albus.hagrid.util.DataLoaderUtil;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class EnvironmentParametersHandler {

    private final Map<String, EnvironmentParameters> environmentParametersMap;

    public EnvironmentParametersHandler(EnvironmentParametersConfig environmentParametersConfig) {
        this.environmentParametersMap = DataLoaderUtil.loadDataMap(
                environmentParametersConfig.dirPath,
                EnvironmentParameters.class
        );
    }

    public EnvironmentParameters get(String environment) {
        EnvironmentParameters environmentParameters = environmentParametersMap.get(environment);
        if (environmentParameters == null) {
            throw new HagridException(String.format("Unknown environment %s", environment));
        }

        return environmentParameters;
    }

    public String getCommandTemplate(String environment) {
        log.info("Get command template[environment = {}]", environment);
        EnvironmentParameters environmentParameters = get(environment);
        return environmentParameters.commandTemplate();
    }

    public List<String> getEnvironmentVariables(String environment) {
        log.info("Get environment variables[environment = {}]", environment);
        EnvironmentParameters environmentParameters = get(environment);
        return environmentParameters.environmentVariables();
    }

    public List<String> getExecutionVariableArgs(String environment) {
        log.info("Get execution variable args[environment = {}]", environment);
        EnvironmentParameters environmentParameters = get(environment);
        return environmentParameters.executionVariableArgs();
    }

    public Map<String, String> getEnvironmentVariableRules(String environment) {
        log.info("Get environment variable rules[environment = {}]", environment);
        EnvironmentParameters environmentParameters = get(environment);
        return environmentParameters.environmentVariableRules();
    }

    public Map<String, String> getExecutionVariableRules(String environment) {
        log.info("Get execution variable rules[environment = {}]", environment);
        EnvironmentParameters environmentParameters = get(environment);
        return environmentParameters.executionVariableRules();
    }
}

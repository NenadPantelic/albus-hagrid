package rs.ac.kg.fin.albus.hagrid.container;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import rs.ac.kg.fin.albus.hagrid.data.container.ContainerRunArgs;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class PostgresContainerManager extends GenericContainerManager implements ContainerManager {

    public GenericContainer createRawContainer(ContainerRunArgs containerRunArgs) {
        Map<String, String> envVariables = containerRunArgs.environmentVariables();

        String username = getEnvVariableOrThrowException(envVariables, "POSTGRES_USER");
        String password = getEnvVariableOrThrowException(envVariables, "POSTGRES_PASSWORD");
        String database = getEnvVariableOrThrowException(envVariables, "POSTGRES_DB");

        return new PostgreSQLContainer(containerRunArgs.image())
                .withDatabaseName(database)
                .withUsername(username)
                .withPassword(password);
    }

    private String getEnvVariableOrThrowException(Map<String, String> envVariables, String key) {
        String value = envVariables.get(key);
        if (value != null) {
            return value;
        }

        throw new NoSuchElementException(
                String.format("Required env variable %s not provided upon creating a container", key)
        );
    }
}

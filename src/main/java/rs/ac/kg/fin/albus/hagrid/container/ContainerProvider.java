package rs.ac.kg.fin.albus.hagrid.container;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.google.common.cache.Cache;
import org.testcontainers.shaded.com.google.common.cache.CacheBuilder;
import rs.ac.kg.fin.albus.hagrid.data.container.ContainerRunArgs;
import rs.ac.kg.fin.albus.hagrid.data.container.GenericContainerWrapper;
import rs.ac.kg.fin.albus.hagrid.data.container.PermanentContainer;
import rs.ac.kg.fin.albus.hagrid.data.container.TemporaryContainerWrapper;
import rs.ac.kg.fin.albus.hagrid.data.environment.EnvironmentType;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class ContainerProvider {

    private static final ContainerManager CONTAINER_MANAGER = new GenericContainerManager();
    private static final ContainerManager PG_CONTAINER_MANAGER = new PostgresContainerManager();

    private static final String POSTGRES_ENVIRONMENT = "postgres";

    private Cache<String, GenericContainerWrapper> containerCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.of(3, ChronoUnit.HOURS))
            .build();

    public GenericContainerWrapper getContainer(String environment,
                                                EnvironmentType environmentType,
                                                ContainerRunArgs containerRunArgs) {

        log.info("Get container[environment = {}, environmentType = {}]", environment, environmentType);
        if (environmentType == EnvironmentType.SINGLE_USE) {
            return createContainer(environment, environmentType, containerRunArgs);
        }

        try {
            return containerCache.get(
                    environment, () -> createContainer(environment, environmentType, containerRunArgs)
            );
        } catch (ExecutionException e) {
            throw new HagridException(
                    String.format("Unable to create a container due to %s", e.getMessage())
            );
        }
    }

    private GenericContainerWrapper createContainer(String environment,
                                                    EnvironmentType environmentType,
                                                    ContainerRunArgs containerRunArgs) {
        ContainerManager containerManager = getContainerManager(environment);
        GenericContainer container = containerManager.runContainer(containerRunArgs);

        // beautify this into if-else ladder
        if (environmentType == EnvironmentType.REUSABLE) {
            return new PermanentContainer(container);
        } else {
            return new TemporaryContainerWrapper(container);
        }
    }

    private ContainerManager getContainerManager(String environment) {
        if (POSTGRES_ENVIRONMENT.equals(environment)) {
            return PG_CONTAINER_MANAGER;
        }

        return CONTAINER_MANAGER;
    }
}



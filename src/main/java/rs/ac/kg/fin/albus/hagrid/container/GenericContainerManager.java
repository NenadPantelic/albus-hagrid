package rs.ac.kg.fin.albus.hagrid.container;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import rs.ac.kg.fin.albus.hagrid.data.container.ContainerRunArgs;
import rs.ac.kg.fin.albus.hagrid.data.container.FileBind;
import rs.ac.kg.fin.albus.hagrid.data.container.FileToCopy;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;

@Slf4j
@Component
public class GenericContainerManager implements ContainerManager {

    @Override
    public GenericContainer runContainer(ContainerRunArgs containerRunArgs) {
        log.info("Start container from {}", containerRunArgs);

        try {
            GenericContainer container = createRawContainer(containerRunArgs);
            container.withEnv(containerRunArgs.environmentVariables());

            // define mounted files
            for (FileBind fileBind : containerRunArgs.fileBinds()) {
                container.withFileSystemBind(fileBind.hostLocations(), fileBind.targetLocation(), fileBind.bindMode());
            }

            if (containerRunArgs.portBindings() != null && !containerRunArgs.portBindings().isEmpty()) {
                container.setPortBindings(containerRunArgs.portBindings());
            }

            container.start();
            log.info("Container {}/{} has successfully started", container.getContainerId(), container.getContainerName());
            // define which files should be copied to container
            // NOTE: container must be running or created, so
            for (FileToCopy fileToCopy : containerRunArgs.filesToCopy()) {
                container.copyFileToContainer(MountableFile.forHostPath(fileToCopy.hostFile()), fileToCopy.targetLocation());
            }

            return container;
        } catch (RuntimeException e) {
            log.info("Failed {}", e.getMessage(), e);
            throw new HagridException(String.format("Unable to start the container due to %s", e.getMessage()));
        }
    }

    protected GenericContainer createRawContainer(ContainerRunArgs containerRunArgs) {
        return new GenericContainer(DockerImageName.parse(containerRunArgs.image()));
    }
}

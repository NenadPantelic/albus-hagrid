package rs.ac.kg.fin.albus.hagrid.data.container;

import com.github.dockerjava.api.command.KillContainerCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class TemporaryContainerWrapper implements GenericContainerWrapper {

    protected final GenericContainer container;

    public TemporaryContainerWrapper(GenericContainer container) {
        this.container = container;
    }


    @Override
    public Container.ExecResult executeCommand(String command) {
        log.info("Trying to execute code {} in {}/{}", command, container.getContainerId(), container.getContainerName());

        try {
            String[] commandPieces = command.split(" ");
            System.out.println(Arrays.toString(commandPieces));
            Container.ExecResult execResult = container.execInContainer(commandPieces);
            log.info("Container {} executed a command {} with the following exec result: stdout = {}, stderr = {}, exit code = {}",
                    container.getContainerId(), command, execResult.getStdout(), execResult.getStderr(), execResult.getExitCode()
            );
            return execResult;
        } catch (IOException | InterruptedException e) {
            log.error("Unable to execute the command {} in container {} due to {}",
                    command, container.getContainerId(), e.getMessage(), e
            );
            throw new HagridException(String.format("Unable to execute the command due to %s", e.getMessage()));
        }
    }

    @Override
    public void clean() {
        String containerId = container.getContainerId();
        log.info("Cleaning up container {}", containerId);
        // kill container
        try (KillContainerCmd killContainerCmd = container.getDockerClient().killContainerCmd(containerId)) {
            killContainerCmd.exec();
        } catch (RuntimeException e) {
            log.error("Unable to kill the container {}", containerId);
            return;
        }

        // remove container
        try (RemoveContainerCmd removeContainerCmd = container.getDockerClient().removeContainerCmd(containerId)) {
            removeContainerCmd.exec();
        } catch (RuntimeException e) {
            log.error("Unable to remove the container {}", containerId);
        }
    }
}

package rs.ac.kg.fin.albus.hagrid.container;

import org.testcontainers.containers.GenericContainer;
import rs.ac.kg.fin.albus.hagrid.data.container.ContainerRunArgs;

public interface ContainerManager {

    GenericContainer runContainer(ContainerRunArgs containerRunArgs);

}

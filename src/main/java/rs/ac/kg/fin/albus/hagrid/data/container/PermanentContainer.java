package rs.ac.kg.fin.albus.hagrid.data.container;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

@Slf4j
public class PermanentContainer extends TemporaryContainerWrapper implements GenericContainerWrapper {

    public PermanentContainer(GenericContainer container) {
        super(container);
    }

    @Override
    public void clean() {
        String containerId = container.getContainerId();
        log.info("Container {} will not be cleaned up, since it is a permament container", containerId);
    }
}

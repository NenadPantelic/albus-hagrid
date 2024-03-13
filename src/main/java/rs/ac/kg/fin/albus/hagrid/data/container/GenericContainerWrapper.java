package rs.ac.kg.fin.albus.hagrid.data.container;

import org.testcontainers.containers.Container;

public interface GenericContainerWrapper {

    Container.ExecResult executeCommand(String command);

    void clean();
}


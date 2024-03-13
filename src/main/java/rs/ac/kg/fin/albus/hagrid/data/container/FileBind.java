package rs.ac.kg.fin.albus.hagrid.data.container;

import org.testcontainers.containers.BindMode;

public record FileBind(String hostLocations,
                       String targetLocation,
                       BindMode bindMode) {

    public static FileBind readOnlyFileBind(String hostFile, String targetLocation) {
        return new FileBind(hostFile, targetLocation, BindMode.READ_ONLY);
    }
}

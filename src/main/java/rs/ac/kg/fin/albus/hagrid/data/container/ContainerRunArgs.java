package rs.ac.kg.fin.albus.hagrid.data.container;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record ContainerRunArgs(String image,
                               String name,
                               Map<String, String> environmentVariables,
                               // hostPort:containerPort format
                               List<String> portBindings,
                               FileBind[] fileBinds,
                               FileToCopy[] filesToCopy) {
}

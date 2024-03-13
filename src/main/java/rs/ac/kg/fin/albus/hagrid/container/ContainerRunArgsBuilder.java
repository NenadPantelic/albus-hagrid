package rs.ac.kg.fin.albus.hagrid.container;


import org.springframework.stereotype.Component;
import rs.ac.kg.fin.albus.hagrid.config.InitParametersConfig;
import rs.ac.kg.fin.albus.hagrid.data.container.ContainerRunArgs;
import rs.ac.kg.fin.albus.hagrid.data.container.FileBind;
import rs.ac.kg.fin.albus.hagrid.data.container.FileToCopy;
import rs.ac.kg.fin.albus.hagrid.data.environment.EnvironmentParameters;

import java.util.Map;

@Component
public class ContainerRunArgsBuilder {

    private final String initFilesDir;

    public ContainerRunArgsBuilder(InitParametersConfig initParametersConfig) {
        this.initFilesDir = initParametersConfig.dirPath;
    }

    public ContainerRunArgs buildContainerRunArgs(String assignmentId,
                                                  EnvironmentParameters environmentParameters,
                                                  String[] codeFilePaths,
                                                  Map<String, String> environmentVariables) {
        ContainerRunArgs.ContainerRunArgsBuilder builder = ContainerRunArgs.builder()
                .image(environmentParameters.image())
                .environmentVariables(environmentVariables);

        String initFilesContainerLocation = environmentParameters.initFilesContainerLocation();
        if (initFilesContainerLocation != null) {
            builder.fileBinds(
                    createEnvironmentFileBinds(assignmentId, initFilesContainerLocation)
            );
        } else {
            builder.fileBinds(new FileBind[]{}); // to avoid NPE; TODO: do it better
        }

        String executionFilesLocation = environmentParameters.executionFilesLocation();
        if (executionFilesLocation != null) {
            builder.filesToCopy(createFilesToCopy(codeFilePaths, executionFilesLocation));
        } else {
            builder.filesToCopy(new FileToCopy[]{}); // to avoid NPE; TODO: do it better
        }

        return builder.build();
    }

    private FileBind[] createEnvironmentFileBinds(String assignmentId, String containerLocation) {
        String hostLocation = String.format("%s/%s", initFilesDir, assignmentId);
        return new FileBind[]{FileBind.readOnlyFileBind(hostLocation, containerLocation)};
    }

    private FileToCopy[] createFilesToCopy(String[] codeFilePaths, String containerLocation) {
        FileToCopy[] filesToCopy = new FileToCopy[codeFilePaths.length];

        for (int i = 0; i < codeFilePaths.length; i++) {
            filesToCopy[i] = new FileToCopy(codeFilePaths[i], containerLocation);
        }

        return filesToCopy;
    }

}

package rs.ac.kg.fin.albus.hagrid.data.environment;

import java.util.List;
import java.util.Map;

public record EnvironmentParameters(String commandTemplate,
                                    List<String> environmentVariables,
                                    List<String> executionVariableArgs,
                                    Map<String, String> environmentVariableRules,
                                    Map<String, String> executionVariableRules,
                                    String initFilesContainerLocation,
                                    String executionFilesLocation,
                                    String image,
                                    EnvironmentType environmentType,
                                    String extension,
                                    Boolean hasTestCases) {
}

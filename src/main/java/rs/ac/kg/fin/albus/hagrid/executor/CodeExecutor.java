package rs.ac.kg.fin.albus.hagrid.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.Container;
import rs.ac.kg.fin.albus.hagrid.code.CodeFileHandler;
import rs.ac.kg.fin.albus.hagrid.config.TestCasesParametersConfig;
import rs.ac.kg.fin.albus.hagrid.container.ContainerProvider;
import rs.ac.kg.fin.albus.hagrid.container.ContainerRunArgsBuilder;
import rs.ac.kg.fin.albus.hagrid.data.container.CodeExecutionResult;
import rs.ac.kg.fin.albus.hagrid.data.container.ContainerRunArgs;
import rs.ac.kg.fin.albus.hagrid.data.container.GenericContainerWrapper;
import rs.ac.kg.fin.albus.hagrid.data.environment.EnvironmentParameters;
import rs.ac.kg.fin.albus.hagrid.environment.CommandParser;
import rs.ac.kg.fin.albus.hagrid.environment.EnvironmentParametersHandler;
import rs.ac.kg.fin.albus.hagrid.environment.EnvironmentVariablesHandler;
import rs.ac.kg.fin.albus.hagrid.environment.ExecutionVariablesHandler;
import rs.ac.kg.fin.albus.hagrid.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CodeExecutor {

    private static final String CODE_FILE_ENV_VARIABLE = "CODE_FILE";
    private static final String TEST_CASES_ENV_VARIABLE = "TEST_CASES";

    private final ContainerRunArgsBuilder containerRunArgsBuilder;
    private final ContainerProvider containerProvider;

    private final CodeFileHandler codeFileHandler;
    private final TestCasesParametersConfig testCasesParametersConfig;
    private final CommandParser commandParser;

    private final EnvironmentParametersHandler environmentParametersHandler;
    private final EnvironmentVariablesHandler environmentVariablesHandler;
    private final ExecutionVariablesHandler executionVariablesHandler;


    public CodeExecutor(ContainerRunArgsBuilder containerRunArgsBuilder,
                        ContainerProvider containerProvider,
                        CodeFileHandler codeFileHandler,
                        TestCasesParametersConfig testCasesParametersConfig,
                        CommandParser commandParser,
                        EnvironmentParametersHandler environmentParametersHandler,
                        EnvironmentVariablesHandler environmentVariablesHandler,
                        ExecutionVariablesHandler executionVariablesHandler) {
        this.containerRunArgsBuilder = containerRunArgsBuilder;
        this.containerProvider = containerProvider;
        this.codeFileHandler = codeFileHandler;
        this.testCasesParametersConfig = testCasesParametersConfig;
        this.commandParser = commandParser;
        this.environmentParametersHandler = environmentParametersHandler;
        this.environmentVariablesHandler = environmentVariablesHandler;
        this.executionVariablesHandler = executionVariablesHandler;
    }

    // TODO: too scripting method; large cognitive complexity of the class
    // TODO: handle images without entrypoint cmd as they will exit immediately
    public CodeExecutionResult execute(String submissionId, String assignmentId, String environment, String code) {
        log.info(
                "Execute code for the following parameters: submissionId = {}, assignmentId = {}, environment = {}",
                submissionId, assignmentId, environment
        );

        Path codeFile = null;
        GenericContainerWrapper container = null;

        try {
            EnvironmentParameters environmentParameters = environmentParametersHandler.get(environment);
            Map<String, String> environmentVariables = environmentVariablesHandler.generateEnvironmentVariables(environment);
            String executionFilesLocation = environmentParameters.executionFilesLocation();

            codeFile = codeFileHandler.createFileWithContent(
                    testCasesParametersConfig.execScriptsDirPath,
                    environment,
                    assignmentId,
                    submissionId,
                    environmentParameters.extension(),
                    code
            );

            environmentVariables.put(CODE_FILE_ENV_VARIABLE, getTargetPath(
                    executionFilesLocation, codeFile.getFileName().toString())
            );

            String[] filesToCopy;
            if (Boolean.TRUE.equals(environmentParameters.hasTestCases())) {
                String testCaseFilesDir = getTargetPath(testCasesParametersConfig.dirPath, assignmentId);
                environmentVariables.put(
                        TEST_CASES_ENV_VARIABLE, testCaseFilesDir
                );

                filesToCopy = new String[]{codeFile.toString(), testCaseFilesDir};
            } else {
                filesToCopy = new String[]{codeFile.toString()};
            }

            List<String> executionVariables = executionVariablesHandler.getExecutionVariables(environment, environmentVariables);
            String command = commandParser.parseCommand(environment, executionVariables);

            ContainerRunArgs containerRunArgs = containerRunArgsBuilder.buildContainerRunArgs(
                    assignmentId,
                    environmentParameters,
                    filesToCopy,
                    environmentVariables
            );

            container = containerProvider.getContainer(
                    environment, environmentParameters.environmentType(), containerRunArgs
            );

            Container.ExecResult commandExecutionResult = container.executeCommand(command);

            return new CodeExecutionResult(commandExecutionResult.getStdout(), commandExecutionResult.getStderr());
        } catch (Exception e) {
            log.error("Something went wrong, code has not been successfully executed: {}", e.getMessage(), e);
            return new CodeExecutionResult(null, "Something went wrong, code has not been successfully executed.");
        } finally {
            if (container != null) {
                container.clean();
            }

            if (codeFile != null) {
                FileUtil.deleteFile(codeFile.toFile());
            }
        }

    }

    private String getTargetPath(String prefix, String name) {
        return Paths.get(prefix, name).toString();
    }
}

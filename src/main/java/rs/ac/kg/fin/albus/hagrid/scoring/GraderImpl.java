package rs.ac.kg.fin.albus.hagrid.scoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rs.ac.kg.fin.albus.hagrid.config.ScoringParametersConfig;
import rs.ac.kg.fin.albus.hagrid.data.scoring.*;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;
import rs.ac.kg.fin.albus.hagrid.util.DataLoaderUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GraderImpl implements Grader {

    private static final String TEST_CASES_SEPARATOR = Pattern.quote("||||>>>>>>>>>>>||||");
    private final Map<String, ScoringParameters> scoringParametersMap;

    public GraderImpl(ScoringParametersConfig scoringParametersConfig) {
        scoringParametersMap = DataLoaderUtil.loadDataMap(
                scoringParametersConfig.getDirPath(),
                ScoringParameters.class
        );
    }

    @Override
    public SubmissionGradingResult grade(String submissionId,
                                         String assignmentId,
                                         String userId,
                                         String valueToCompare) {
        log.info("Grade submission[id = {}, assignmentId = {}, userId = {}]", submissionId, assignmentId, userId);

        ScoringParameters scoringParameters = getScoringParameters(assignmentId);
        Map<String, String> executionResultsByTestCases = parseExecutionResult(
                valueToCompare, scoringParameters.linesToSkip()
        );

        GradingResult gradingResult = gradeTestCases(
                executionResultsByTestCases,
                scoringParameters.solutions(),
                scoringParameters.maxScore(),
                scoringParameters.scoringPolicy()
        );

        return SubmissionGradingResult.builder()
                .submissionId(submissionId)
                .assignmentId(assignmentId)
                .userId(userId)
                .gradingResult(gradingResult)
                .build();
    }

    private Map<String, String> parseExecutionResult(String executionResult, String[] linesToSkip) {
        // Output format
        // Test case #{no}
        // Output:
        // ||||>>>>>>>>>>>||||
        String processedExecutionResult = processExecutionResult(executionResult, linesToSkip);
        String[] testCaseResults = processedExecutionResult.split(TEST_CASES_SEPARATOR);
        if (testCaseResults.length < 2) {
            return Map.of("1", processedExecutionResult);
        }

        Map<String, String> testCaseExecutionResults = new HashMap<>();
        for (String testCase : testCaseResults) {
            String[] testCaseDetails = testCase.split("\n");

            System.out.println(testCaseDetails[0]);
            System.out.println(testCaseDetails[11]);
        }

        return testCaseExecutionResults;

    }

    // TODO: currently, we can hold complete data in-memory, but later on, using cache instead of map here
    // will be handy
    private ScoringParameters getScoringParameters(String assignmentId) {
        log.info("Get scoring parameters[assignmentId = {}]", assignmentId);

        ScoringParameters scoringParameters = scoringParametersMap.get(assignmentId);
        if (scoringParameters == null) {
            throw new HagridException(
                    String.format("Could not find scoring parameters for assignment %s", assignmentId)
            );
        }

        return scoringParameters;
    }

    // TODO: leave an explanation
    private String processExecutionResult(String executionResult, String[] linesToSkip) {
        String[] executionResultSegments = executionResult.split("\n");
        Set<Integer> normalizedLinesToSkip = getSetOfNormalizedLinesToSkip(executionResultSegments.length, linesToSkip);

        List<String> linesToInclude = new ArrayList<>(
                executionResultSegments.length - normalizedLinesToSkip.size()
        ) {
        };

        for (int i = 0; i < executionResultSegments.length; i++) {
            if (!normalizedLinesToSkip.contains(i)) {
                linesToInclude.add(executionResultSegments[i]);
            }
        }

        return String.join("\n", linesToInclude);
    }

    private Set<Integer> getSetOfNormalizedLinesToSkip(int numOfLinesInContent, String[] linesToSkip) {
        Set<Integer> setOfLinesToSkip = new HashSet<>();
        String numOfLinesStringified = String.valueOf(numOfLinesInContent);

        for (String lineToSkip : linesToSkip) {
            int normalizedLineToSkip = Integer.parseInt(lineToSkip.replace("n", numOfLinesStringified)) - 1;
            setOfLinesToSkip.add(normalizedLineToSkip);
        }

        return setOfLinesToSkip;
    }

    private GradingResult gradeTestCases(Map<String, String> executionResults,
                                         Map<String, String> expectedResults,
                                         float totalScore,
                                         ScoringPolicy scoringPolicy) {

        float score = 0.0f;
        List<TestCaseResult> testCaseResults = new ArrayList<>();

        for (Map.Entry<String, String> entry : expectedResults.entrySet()) {
            String testCaseId = entry.getKey();
            String expectedValue = entry.getValue();
            String actualValue = executionResults.get(testCaseId);
            float scoringResult = doCompare(expectedValue, actualValue, scoringPolicy) * totalScore;
            score += scoringResult;
            testCaseResults.add(
                    new TestCaseResult(testCaseId, actualValue, expectedValue, scoringResult)
            );
        }

        return GradingResult.builder()
                .testCaseResults(testCaseResults)
                .score(score)
                .total(totalScore)
                .build();
    }

    private float doCompare(String expectedValue, String actualValue, ScoringPolicy scoringPolicy) {
        if (scoringPolicy == ScoringPolicy.BINARY) {
            return expectedValue.equals(actualValue) ? 1.0f : 0.0f;
        }

        String[] expectedValueLines = expectedValue.split("\n");
        String[] actualValueLines = actualValue.split("\n");

        int expectedValueLinesCnt = expectedValueLines.length;
        int actualValueLinesCnt = actualValueLines.length;

        int numOfLinesToProcess = expectedValueLinesCnt;
        if (actualValueLinesCnt > numOfLinesToProcess) {
            numOfLinesToProcess = actualValueLinesCnt;
        }

        int matchedLines = 0;
        for (int i = 0; i < numOfLinesToProcess; i++) {
            String valueA = getElementAt(expectedValueLines, i);
            String valueB = getElementAt(actualValueLines, i);

            if (isBlank(valueA) && isBlank(valueB) && valueA.equals(valueB)) {
                matchedLines++;
            }
        }

        return BigDecimal.valueOf(11.0f * matchedLines / expectedValueLinesCnt).floatValue();

    }

    private String getElementAt(String[] values, int index) {
        if (index >= values.length) {
            return null;
        }

        return values[index];
    }

    private boolean isBlank(String value) {
        return value != null && !value.isBlank();
    }

}

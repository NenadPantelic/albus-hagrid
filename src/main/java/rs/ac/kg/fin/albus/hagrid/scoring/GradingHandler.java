package rs.ac.kg.fin.albus.hagrid.scoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.kg.fin.albus.hagrid.data.container.CodeExecutionResult;
import rs.ac.kg.fin.albus.hagrid.data.scoring.GradingResult;
import rs.ac.kg.fin.albus.hagrid.data.scoring.SubmissionGradingResult;
import rs.ac.kg.fin.albus.hagrid.executor.CodeExecutor;

@Slf4j
@Service
public class GradingHandler {

    private final CodeExecutor codeExecutor;
    private final Grader grader;

    public GradingHandler(CodeExecutor codeExecutor, Grader grader) {
        this.codeExecutor = codeExecutor;
        this.grader = grader;
    }

    public void executeAndGrade() {
        // TODO: get data from Kafka
        // Postgres
          String assignmentId = "123";
          String submissionId = "subm-123";
          String userId = "user-123";
          String environment = "postgres";
          String code = "select * from Highschooler";

        // Python
        // String assignmentId = "456";
        // String submissionId = "subm-456";
        // String userId = "user-456";
        // String environment = "python";
//        String code = """
//                class Solution:
//                    def run(self, a, b):
//                        return a, b
//                """;

        CodeExecutionResult codeExecutionResult = codeExecutor.execute(
                submissionId, assignmentId, environment, code
        );

        SubmissionGradingResult submissionGradingResult;

        if (codeExecutionResult.hasError()) {
            submissionGradingResult = SubmissionGradingResult.builder()
                    .submissionId(submissionId)
                    .assignmentId(assignmentId)
                    .userId(userId)
                    .gradingResult(new GradingResult(null, 0.0f, 0.0f))
                    .build();
        } else {
            submissionGradingResult = grader.grade(submissionId, assignmentId, userId, codeExecutionResult.output());
        }
        System.out.println(submissionGradingResult);
        // send to kafka
    }
}

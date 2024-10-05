package rs.ac.kg.fin.albus.hagrid.scoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import rs.ac.kg.fin.albus.hagrid.data.container.CodeExecutionResult;
import rs.ac.kg.fin.albus.hagrid.data.scoring.GradingResult;
import rs.ac.kg.fin.albus.hagrid.data.scoring.SubmissionGradingResult;
import rs.ac.kg.fin.albus.hagrid.event.config.KafkaConfig;
import rs.ac.kg.fin.albus.hagrid.event.data.CodeSubmission;
import rs.ac.kg.fin.albus.hagrid.executor.CodeExecutor;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GradingHandler {

    private final CodeExecutor codeExecutor;
    private final Grader grader;

    private final KafkaTemplate<String, SubmissionGradingResult> kafkaTemplate;

    public final String producerTopic;

    public GradingHandler(CodeExecutor codeExecutor,
                          Grader grader,
                          KafkaTemplate<String, SubmissionGradingResult> kafkaTemplate,
                          KafkaConfig kafkaConfig) {
        this.codeExecutor = codeExecutor;
        this.grader = grader;
        this.kafkaTemplate = kafkaTemplate;
        this.producerTopic = kafkaConfig.producerTopic();
    }

    @KafkaListener(topics = {"code-submission-events"}, groupId = "code-submission-events-listener-group")
    public void handleCodeSubmission(ConsumerRecord<String, CodeSubmission> consumerRecord) {
        String submissionId = consumerRecord.key();
        CodeSubmission codeSubmission = consumerRecord.value();
        log.info(
                "Consuming code submission[submissionId = {}, userId = {}, assignmentId = {}, environment = {}]",
                submissionId, codeSubmission.userId(), codeSubmission.assignmentId(), codeSubmission.environment()
        );

        String assignmentId = codeSubmission.assignmentId();
        String userId = codeSubmission.userId();
        String environment = codeSubmission.environment();
        String code = codeSubmission.code();

        CodeExecutionResult codeExecutionResult = codeExecutor.execute(submissionId, assignmentId, environment, code);
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

        log.info("Submission grading result: {}", submissionGradingResult);
        try {
            sendSubmissionGradingResult(submissionGradingResult);
        } catch (Exception e) {
            log.error("Unable to send the submission grading result {} due to {}.", submissionGradingResult, e.getMessage(), e);
        }

    }

    private void sendSubmissionGradingResult(SubmissionGradingResult submissionGradingResult) throws JsonProcessingException {
        String key = submissionGradingResult.submissionId();
        CompletableFuture<SendResult<String, SubmissionGradingResult>> completableFuture = kafkaTemplate.send(
                producerTopic, key, submissionGradingResult
        );

        completableFuture.whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                handleFailure(key, submissionGradingResult, throwable);
            } else {
                handleSuccess(key, submissionGradingResult, sendResult);
            }
        });
    }

    private void handleSuccess(String key,
                               SubmissionGradingResult submissionGradingResult,
                               SendResult<String, SubmissionGradingResult> sendResult) {
        log.info(
                "Message sent successfully for: key = {}, value = {}, partition = {}",
                key, submissionGradingResult, sendResult.getRecordMetadata().partition()
        );
    }

    private void handleFailure(String key, SubmissionGradingResult submissionGradingResult, Throwable throwable) {
        log.error(
                "An error occurred when sending the message {}/{} due to {}",
                key, submissionGradingResult, throwable.getMessage(), throwable
        );

    }
}

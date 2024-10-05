package rs.ac.kg.fin.albus.hagrid.scoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final CodeExecutor codeExecutor;
    private final Grader grader;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public final String producerTopic;

    public GradingHandler(CodeExecutor codeExecutor,
                          Grader grader,
                          KafkaTemplate<String, String> kafkaTemplate,
                          KafkaConfig kafkaConfig) {
        this.codeExecutor = codeExecutor;
        this.grader = grader;
        this.kafkaTemplate = kafkaTemplate;
        this.producerTopic = kafkaConfig.producerTopic();
    }

    @KafkaListener(topics = {"code-submission-events"}, groupId = "code-submission-events-listener-group")
    public void handleCodeSubmission(ConsumerRecord<String, String> consumerRecord) {
        String submissionId = consumerRecord.key();
        CodeSubmission codeSubmission = deserializeCodeSubmissionPayload(consumerRecord.value());
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
        String eventPayload = serializeSubmissionGradingResult(submissionGradingResult);
        CompletableFuture<SendResult<String, String>> completableFuture = kafkaTemplate.send(
                producerTopic, key, eventPayload
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
                               SendResult<String, String> sendResult) {
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

    private CodeSubmission deserializeCodeSubmissionPayload(String payload) {
        try {
            return OBJECT_MAPPER.readValue(payload, CodeSubmission.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeSubmissionGradingResult(SubmissionGradingResult submissionGradingResult) {
        try {
            return OBJECT_MAPPER.writeValueAsString(submissionGradingResult);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize the submission grading result {} due to {}", submissionGradingResult, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

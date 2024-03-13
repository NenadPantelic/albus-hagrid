package rs.ac.kg.fin.albus.hagrid.data.scoring;

import lombok.Builder;

@Builder
public record SubmissionGradingResult(String submissionId,
                                      String assignmentId,
                                      String userId,
                                      GradingResult gradingResult) {

}

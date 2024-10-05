package rs.ac.kg.fin.albus.hagrid.event.data;

public record CodeSubmission(String userId,
                             String assignmentId,
                             String submissionId,
                             String environment,
                             String code) {
}

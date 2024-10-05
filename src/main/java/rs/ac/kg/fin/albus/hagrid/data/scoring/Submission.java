package rs.ac.kg.fin.albus.hagrid.data.scoring;

public record Submission(String eventId,
                         String assignmentId,
                         String submissionId,
                         String userId,
                         String environment,
                         String code) {
}

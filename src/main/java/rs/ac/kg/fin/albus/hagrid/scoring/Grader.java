package rs.ac.kg.fin.albus.hagrid.scoring;

import rs.ac.kg.fin.albus.hagrid.data.scoring.SubmissionGradingResult;

public interface Grader {

    SubmissionGradingResult grade(String submissionId,
                                  String assignmentId,
                                  String userId,
                                  String valueToCompare);
}

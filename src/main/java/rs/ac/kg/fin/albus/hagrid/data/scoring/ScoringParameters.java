package rs.ac.kg.fin.albus.hagrid.data.scoring;

import java.util.Map;

public record ScoringParameters(String assignmentId,
                                ScoringPolicy scoringPolicy,
                                float maxScore,
                                Map<String, String> solutions,
                                String[] linesToSkip) {
}


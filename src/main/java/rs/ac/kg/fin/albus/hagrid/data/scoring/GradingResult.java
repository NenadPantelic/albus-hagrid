package rs.ac.kg.fin.albus.hagrid.data.scoring;

import lombok.Builder;

import java.util.List;

@Builder
public record GradingResult(List<TestCaseResult> testCaseResults,
                            float score,
                            float total) {
}

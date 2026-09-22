package com.sih.innovationplatform.match;

import com.sih.innovationplatform.ai.AiModels;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems/{problemId}")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/match")
    public List<MatchDto> runMatching(@PathVariable Long problemId) {
        return matchingService.runMatching(problemId);
    }

    @GetMapping("/matches")
    public List<MatchDto> matches(
            @PathVariable Long problemId,
            @RequestParam(required = false) String type) {
        return matchingService.matchesFor(problemId, type);
    }

    @PostMapping("/recommend")
    public AiModels.Recommendation recommend(@PathVariable Long problemId) {
        return matchingService.recommend(problemId);
    }

    @GetMapping("/recovery-recommendations")
    public AiModels.RecoveryRecommendation recovery(@PathVariable Long problemId,
                                                    @RequestParam String missingCapability) {
        return matchingService.recovery(problemId, missingCapability);
    }
}
package com.sih.innovationplatform.problem;

import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;
    private final CurrentUser currentUser;

    @PostMapping
    public ProblemResponse create(@RequestBody ProblemCreateRequest request) {
        User user = currentUser.get();
        return problemService.create(request, user.getId());
    }

    @GetMapping
    public List<ProblemResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q) {
        return problemService.list(status, category, q);
    }

    @GetMapping("/{id}")
    public ProblemResponse get(@PathVariable Long id) {
        return problemService.get(id);
    }

    @PutMapping("/{id}")
    public ProblemResponse update(@PathVariable Long id,
                                  @RequestBody ProblemUpdateRequest request) {
        User user = currentUser.get();
        return problemService.update(id, request, user.getId());
    }

    @PostMapping("/{id}/analyze")
    public ProblemResponse analyze(@PathVariable Long id) {
        problemService.runAnalysis(id);
        return problemService.get(id);
    }

    @PostMapping("/{id}/verify")
    public Map<String, Object> verify(@PathVariable Long id) {
        problemService.verify(id);
        return Map.of("status", Problem.STATUS_VERIFIED, "id", id);
    }

    @PostMapping("/{id}/merge")
    public Map<String, Object> merge(@PathVariable Long id,
                                     @RequestBody Map<String, Long> body) {
        Long targetId = body.get("targetProblemId");
        problemService.merge(id, targetId);
        return Map.of("status", Problem.STATUS_MERGED, "id", id, "mergedInto", targetId);
    }

    @GetMapping("/{id}/capabilities")
    public List<ProblemCapability> capabilities(@PathVariable Long id) {
        return problemService.capabilities(id);
    }
}
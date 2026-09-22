package com.sih.innovationplatform.dashboard;

import com.sih.innovationplatform.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return dashboardService.analytics();
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        com.sih.innovationplatform.user.User user = currentUser.get();
        return Map.of(
                "role", user.getRoleName(),
                "name", user.getName(),
                "email", user.getEmail());
    }
}
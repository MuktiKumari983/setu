package com.sih.innovationplatform.notification;

import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    @GetMapping
    public List<Notification> mine() {
        User user = currentUser.get();
        return notificationService.forUser(user.getId());
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> read(@PathVariable Long id) {
        User user = currentUser.get();
        return notificationService.markRead(id, user.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unread() {
        User user = currentUser.get();
        return Map.of("count", notificationService.unreadCount(user.getId()));
    }
}
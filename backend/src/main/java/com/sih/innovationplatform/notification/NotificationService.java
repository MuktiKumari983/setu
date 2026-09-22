package com.sih.innovationplatform.notification;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final UserRepository userRepo;

    public void push(Long userId, String message, String type, String link) {
        userRepo.findById(userId).ifPresent(u -> {
            Notification n = new Notification();
            n.setUser(u);
            n.setMessage(message);
            n.setType(type == null ? "INFO" : type);
            n.setLink(link);
            repo.save(n);
        });
    }

    public List<Notification> forUser(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Object> markRead(Long notificationId, Long userId) {
        Notification n = repo.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("Not your notification");
        }
        n.setIsRead(true);
        repo.save(n);
        return Map.of("ok", true);
    }

    public long unreadCount(Long userId) {
        return repo.countByUserIdAndIsReadFalse(userId);
    }
}
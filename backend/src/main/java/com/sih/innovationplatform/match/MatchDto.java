package com.sih.innovationplatform.match;

import java.util.List;

public record MatchDto(
        String entityType,
        Long entityId,
        String entityName,
        Double score,
        String explanation,
        List<String> contributingFactors,
        String status
) {
}
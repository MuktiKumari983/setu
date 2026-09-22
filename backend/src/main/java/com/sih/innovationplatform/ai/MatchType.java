package com.sih.innovationplatform.ai;

public enum MatchType {
    UNIVERSITY,
    TEAM,
    MENTOR,
    INDUSTRY;

    public String forAi() {
        return name().toLowerCase();
    }
}
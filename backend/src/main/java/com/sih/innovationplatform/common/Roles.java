package com.sih.innovationplatform.common;

public final class Roles {

    public static final String CITIZEN = "CITIZEN";
    public static final String STUDENT = "STUDENT";
    public static final String FACULTY = "FACULTY";
    public static final String INDUSTRY = "INDUSTRY";
    public static final String UNIVERSITY_ADMIN = "UNIVERSITY_ADMIN";
    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String GOVERNMENT_ADMIN = "GOVERNMENT_ADMIN";

    public static final String[] PLATFORM_ADMINS = {PLATFORM_ADMIN, GOVERNMENT_ADMIN};
    public static final String[] PROBLEM_ADMINS = {PLATFORM_ADMIN, GOVERNMENT_ADMIN, UNIVERSITY_ADMIN};

    private Roles() {
    }
}

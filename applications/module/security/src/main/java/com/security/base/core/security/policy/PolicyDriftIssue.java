package com.security.base.core.security.policy;


public record PolicyDriftIssue(String severity, String type, String endpoint, String method, String authority,
                               String details) {

}


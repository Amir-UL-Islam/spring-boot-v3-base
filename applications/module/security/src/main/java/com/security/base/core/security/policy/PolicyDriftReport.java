package com.security.base.core.security.policy;

import java.time.Instant;
import java.util.List;

public record PolicyDriftReport(Instant generatedAt, int issueCount, List<PolicyDriftIssue> issues) {

}


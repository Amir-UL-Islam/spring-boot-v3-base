package com.security.base.core.security.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Order(20)
@Slf4j
public class PolicyDriftStartupCheck implements ApplicationRunner {

    private final PolicyDriftService policyDriftService;

    @Value("${security.policy.drift.enabled:true}")
    private boolean enabled;

    @Value("${security.policy.drift.fail-on-drift:false}")
    private boolean failOnDrift;

    @Override
    public void run(final ApplicationArguments args) {
        if (!enabled) {
            log.info("policy drift check disabled");
            return;
        }

        final PolicyDriftReport report = policyDriftService.analyzeDrift();
        if (report.issueCount() == 0) {
            log.info("policy drift check passed with no issues");
            return;
        }

        report.issues().forEach(issue ->
                log.warn("policy drift [{}] {} {} authority={} details={}",
                        issue.type(),
                        issue.method(),
                        issue.endpoint(),
                        issue.authority(),
                        issue.details()));

        if (failOnDrift) {
            throw new IllegalStateException("Policy drift check failed with " + report.issueCount() + " issue(s)");
        }
    }
}


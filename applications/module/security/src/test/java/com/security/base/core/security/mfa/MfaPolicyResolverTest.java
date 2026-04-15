package com.security.base.core.security.mfa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.security.base.core.role.model.entity.Role;
import com.security.base.core.security.policy.config.SecurityPolicyKeys;
import com.security.base.core.security.policy.config.SecurityPolicyService;
import com.security.base.core.users.model.entity.Users;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MfaPolicyResolverTest {

    @Mock
    private SecurityPolicyService securityPolicyService;

    private MfaPolicyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MfaPolicyResolver(securityPolicyService);
    }

    @Test
    void shouldRequireMfaWhenRoleIsEnforced() {
        when(securityPolicyService.getCsvOrDefault(SecurityPolicyKeys.MFA_ENFORCED_ROLES, ""))
                .thenReturn(List.of("ADMIN"));
        // These 2 line bellow will throw error if uncommented:
        // Clean & maintainable test code requires zero unnecessary code.
        // Following stubbings are unnecessary (click to navigate to relevant line of code):
        //  1. -> at com.security.base.core.security.mfa.MfaPolicyResolverTest.shouldRequireMfaWhenRoleIsEnforced(MfaPolicyResolverTest.java:37)
        //Please remove unnecessary stubbings or use 'lenient' strictness. More info: javadoc for UnnecessaryStubbingException class.

//        when(securityPolicyService.getBooleanOrDefault(SecurityPolicyKeys.MFA_OPTIONAL_PER_USER, true))
//                .thenReturn(true);

        final Users user = new Users();
        final Role role = new Role();
        role.setName("ADMIN");
        user.setRole(Set.of(role));

        assertTrue(resolver.isMfaRequired(user));
    }

    @Test
    void shouldNotRequireMfaForOptionalUserWithoutEnabledFactor() {
        when(securityPolicyService.getCsvOrDefault(SecurityPolicyKeys.MFA_ENFORCED_ROLES, ""))
                .thenReturn(List.of());
        when(securityPolicyService.getBooleanOrDefault(SecurityPolicyKeys.MFA_OPTIONAL_PER_USER, true))
                .thenReturn(true);

        final Users user = new Users();

        assertFalse(resolver.isMfaRequired(user));
    }

    @Test
    void shouldResolveAllowedFactorsForAdminRole() {
        when(securityPolicyService.getCsvOrDefault(SecurityPolicyKeys.MFA_ALLOWED_FACTORS_ADMIN, "TOTP,SMS,EMAIL"))
                .thenReturn(List.of("TOTP", "EMAIL"));

        final Users user = new Users();
        final Role role = new Role();
        role.setName("ADMIN");
        user.setRole(Set.of(role));

        assertEquals(Set.of(MfaFactorType.TOTP, MfaFactorType.EMAIL), resolver.allowedFactorsForUser(user));
    }
}


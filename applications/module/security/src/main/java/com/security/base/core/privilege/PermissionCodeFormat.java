package com.security.base.core.privilege;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import com.security.base.core.security.oauth.PermissionCodes;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Validates matrix permission naming convention: resource:action.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PermissionCodeFormat.PermissionCodeFormatValidator.class)
public @interface PermissionCodeFormat {

    String message() default "{invalid.permission.code.format}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class PermissionCodeFormatValidator implements ConstraintValidator<PermissionCodeFormat, String> {

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            return PermissionCodes.isSupportedPermissionCode(value);
        }
    }
}


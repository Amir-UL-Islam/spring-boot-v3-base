package com.security.base.role;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.web.servlet.HandlerMapping;


/**
 * Validate that the description value isn't taken yet.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = RoleDescriptionUnique.RoleDescriptionUniqueValidator.class
)
public @interface RoleDescriptionUnique {

    String message() default "{exists.role.description}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class RoleDescriptionUniqueValidator implements ConstraintValidator<RoleDescriptionUnique, String> {

        private final RoleService roleService;
        private final HttpServletRequest request;

        public RoleDescriptionUniqueValidator(final RoleService roleService,
                final HttpServletRequest request) {
            this.roleService = roleService;
            this.request = request;
        }

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext cvContext) {
            if (value == null) {
                // no value present
                return true;
            }
            @SuppressWarnings("unchecked") final Map<String, String> pathVariables =
                    ((Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            final String currentId = pathVariables.get("id");
            if (currentId != null && value.equalsIgnoreCase(roleService.get(Long.parseLong(currentId)).getDescription())) {
                // value hasn't changed
                return true;
            }
            return !roleService.descriptionExists(value);
        }

    }

}

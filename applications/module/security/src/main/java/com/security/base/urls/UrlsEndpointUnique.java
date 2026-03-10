package com.security.base.urls;

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
 * Validate that the endpoint value isn't taken yet.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = UrlsEndpointUnique.UrlsEndpointUniqueValidator.class
)
public @interface UrlsEndpointUnique {

    String message() default "{exists.urls.endpoint}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class UrlsEndpointUniqueValidator implements ConstraintValidator<UrlsEndpointUnique, String> {

        private final UrlService urlsService;
        private final HttpServletRequest request;

        public UrlsEndpointUniqueValidator(final UrlService urlsService,
                final HttpServletRequest request) {
            this.urlsService = urlsService;
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
            if (currentId != null && value.equalsIgnoreCase(urlsService.get(Long.parseLong(currentId)).getEndpoint())) {
                // value hasn't changed
                return true;
            }
            return !urlsService.endpointExists(value);
        }

    }

}

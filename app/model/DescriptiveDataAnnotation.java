package model;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = DescriptiveDataValidator.class)
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DescriptiveDataAnnotation {
    String message() default "{DescriptiveDataAnnotation}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

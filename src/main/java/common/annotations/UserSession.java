package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserSession {
    String prefix() default "User";
    int accounts() default 2;
    String role() default "USER";
    boolean create() default true;
    int users() default 1;
}
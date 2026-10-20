package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserSession {
    // Префикс для имени пользователя
    String prefix() default "User";

    // Количество аккаунтов для создания
    int accounts() default 2;

    // Роль пользователя
    String role() default "USER";

    // Создавать ли пользователя (если false - используем существующего)
    boolean create() default true;
}
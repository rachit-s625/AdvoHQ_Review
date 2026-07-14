package in.advohq.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Controller-parameter shortcut for the authenticated {@link AppUserPrincipal}.
 * Usage: {@code public X handler(@CurrentUser AppUserPrincipal me) { ... }}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}

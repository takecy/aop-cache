/**
 *
 */
package info.takebo.cached.annotation;

import info.takebo.cached.interceptor.CachedInterceptor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * IDを表す引数に付加する
 * {@link Cached},{@link CachedInterceptor}で参照する
 * </pre>
 *
 * @author takecy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
@Documented
public @interface Id {

}

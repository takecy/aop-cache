/**
 *
 */
package info.takebo.cached.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * 戻り値をキャッシュするメソッドに付加
 *
 * {@link Id}と併用する
 * キャッシュ時のKeyに使用する引数に{@link Id}を付加する
 * 少なくともひとつの{@link Id}の指定が必須
 *
 * キャッシュ可能な戻り値の型
 * POJO
 * {@link Optional}
 * {@link List}
 * {@link Set}
 *
 * 戻り値が{@link Map}のメソッドは使用不可
 * </pre>
 *
 * @author takecy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Cached {

	public static enum Action {
		SELECT,
		DELETE;
	}

	/** expireの長さ */
	int expire() default 10;

	/** expireの単位 */
	TimeUnit unit() default TimeUnit.MINUTES;

	/** キャッシュ時のキーprefix */
	String prefix() default "";

	/**
	 * <pre>
	 * 戻り値の型
	 * キャッシュ時の型になる
	 * {@link Optional} or {@link Collection}の時はGenerics部分
	 * </pre>
	 */
	Class<?> genericType();

	/**
	 * キャッシュに対して行う操作
	 *
	 * @return
	 */
	Cached.Action action() default Cached.Action.SELECT;
}

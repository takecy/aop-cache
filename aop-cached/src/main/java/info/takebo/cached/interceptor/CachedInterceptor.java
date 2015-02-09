/**
 *
 */
package info.takebo.cached.interceptor;

import info.takebo.cached.annotation.Cached;
import info.takebo.cached.annotation.Id;
import info.takebo.cached.service.CacheService;
import info.takebo.cached.util.Jsons;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * <pre>
 * cache interceptor
 * </pre>
 *
 * @author takecy
 */
public class CachedInterceptor implements MethodInterceptor {

	@Inject
	private CacheService cacheService;

	private enum ReturnType {
		/** {@link Optional} */
		OPTIONAL,
		/** {@link List} */
		LIST,
		/** {@link Set} */
		SET,
		/** その他 */
		ETC;
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		// 実行対象インスタンスの生成元クラス名(サブクラス名)
		final Class<?> invokeClass = methodInvocation.getThis().getClass();
		String invokeClassName = invokeClass.getSimpleName();
		// Guice情報をsubstring
		invokeClassName = StringUtils.substringBefore(invokeClassName, "$$");

		// 実行対象メソッド
		final Method invokeMethod = methodInvocation.getMethod();
		// 実行対象メソッドが定義されているクラス(継承元クラス名)
		final Class<?> methodDeclaredClass = invokeMethod.getDeclaringClass();
		// 戻り値の型
		final Class<?> returnTypeOriginal = invokeMethod.getReturnType();

		// Objectクラスのメソッドは無視
		if (methodDeclaredClass == Object.class)
			return methodInvocation.proceed();

		// アノテーション情報
		Cached cachedAnnotation = invokeMethod.getAnnotation(Cached.class);
		int expire = cachedAnnotation.expire();
		TimeUnit unit = cachedAnnotation.unit();
		String prefix = cachedAnnotation.prefix();
		Class<?> returnType = cachedAnnotation.genericType();

		// キャッシュ削除の場合
		if (cachedAnnotation.action() == Cached.Action.DELETE) {
			String key = Joiner.on(":").join("aopcached", invokeClassName);
			Set<String> keys = cacheService.listKeys(key + "*");
			if (Iterables.isEmpty(keys) == false) {
				cacheService.delete(keys);
			}
			return methodInvocation.proceed();
		}

		// 戻り値の型判定
		ReturnType type = ReturnType.ETC;

		// 型検査
		{
			if (returnTypeOriginal == Map.class) {
				throw new AssertionError("Map.is.not.support.cached");
			}

			// Optional,List,Set型の場合、戻り値の型指定必須
			if (returnTypeOriginal == Optional.class) {
				type = ReturnType.OPTIONAL;
				if (returnType == null)
					throw new AssertionError("require.returntype.when.optional.type");
			}
			else if (returnTypeOriginal == List.class) {
				type = ReturnType.LIST;
				if (returnType == null)
					throw new AssertionError("require.returntype.when.list.type");
			}
			else if (returnTypeOriginal == Set.class) {
				type = ReturnType.SET;
				if (returnType == null)
					throw new AssertionError("require.returntype.when.set.type");
			}

		}

		// キャッシュのキー指定を探す
		List<String> ids = Lists.newArrayList();
		// メソッド引数一覧
		Object[] args = methodInvocation.getArguments();
		// メソッドの引数アノテーション一覧
		Annotation[][] annotationmap = invokeMethod.getParameterAnnotations();

		for (int i = 0; i < annotationmap.length; i++) {
			Annotation[] annotations = annotationmap[i];
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Id.class) {
					if (args[i] == null)
						continue;
					ids.add(Objects.toString(args[i]));
				}
			}
		}

		// cache有効の時はキャッシュキー指定が少なくとも1つ必須
		if (Iterables.isEmpty(ids))
			throw new AssertionError("key required at least one when the cache enabled");

		// cache get
		String idKey = Joiner.on(":").join(ids);
		String key = Joiner.on(":").join("aopcached", invokeClassName, invokeMethod.getName(), prefix, idKey);

		Optional<String> cachedObject = cacheService.get(key);

		if (cachedObject.isPresent()) {
			switch (type) {
				case OPTIONAL:
					Object obj = Jsons.fromJson(cachedObject.get(), returnType);
					return Optional.of(obj);
				case LIST:
					List<?> list = Jsons.fromJsonToList(cachedObject.get(), returnType);
					return list;
				case SET:
					Set<?> set = Jsons.fromJsonToSet(cachedObject.get(), returnType);
					return set;
				case ETC:
					// fall through
				default:
					return Jsons.fromJson(cachedObject.get(), returnTypeOriginal);
			}
		}

		// 本来の処理
		Object obj = methodInvocation.proceed();

		if (obj == null)
			return obj;

		switch (type) {
			case OPTIONAL:
				Optional<?> op = (Optional<?>) obj;
				if (op.isPresent())
					cacheService.setWithExpire(key, Jsons.toNode(op.get()).toString(), expire, unit);
				break;
			case LIST:
				// fall through
			case SET:
				if (Iterables.isEmpty((Collection<?>) obj) == false)
					cacheService.setWithExpire(key, Jsons.toNode(obj).toString(), expire, unit);
				break;
			case ETC:
				cacheService.setWithExpire(key, Jsons.toNode(obj).toString(), expire, unit);
				break;
			default:
				break;
		}

		return obj;
	}

}

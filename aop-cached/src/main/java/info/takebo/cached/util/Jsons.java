/**
 *
 */
package info.takebo.cached.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * @author yamashita_takeshi
 */
public class Jsons {

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.registerModule(new GuavaModule());
	}

	/**
	 * Jackson取得
	 *
	 * @return
	 */
	public static ObjectMapper getParser() {
		return mapper;
	}

	/**
	 * Mapへ変換する
	 *
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> toMap(Object obj) {
		return mapper.convertValue(obj, TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
	}

	/**
	 * List<Object>へ変換する
	 *
	 * @param list
	 * @return
	 */
	public static List<Object> toObjectList(List<?> list) {
		List<String> stringList = toJsonList(list);
		return Lists.transform(stringList,
								new Function<String, Object>() {
									@Override
									public Object apply(String input) {
										return (Object)input;
									}
								});
	}

	/**
	 * Json -> Object
	 *
	 * @param obj String or JsonNode
	 * @param clazz
	 * @return
	 */
	public static <T> T fromJson(Object obj, Class<T> clazz) {
		Preconditions.checkNotNull(obj);
		Preconditions.checkNotNull(clazz);

		try {
			if (obj instanceof String) {
				return mapper.readValue((String) obj, clazz);
			}
			if (obj instanceof JsonNode) {
				return mapper.readValue(((JsonNode) obj).traverse(), clazz);
			} else {
				throw new IllegalArgumentException("not support type.");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * List<String(Json)> -> List<T>
	 *
	 * @param list
	 * @return
	 */
	public static <T> List<T> fromJson(List<String> list, final Class<T> clazz) {
		Preconditions.checkNotNull(list);

		return Lists.newArrayList(Lists.transform(list, new Function<String, T>() {
			@Override
			public T apply(String json) {
				return Jsons.fromJson(json, clazz);
			}
		}));
	}

	public static <T> List<T> fromJsonToList(String json, Class<T> elementClazz) {
		Preconditions.checkNotNull(json);
		Preconditions.checkNotNull(elementClazz);

		JsonNode node = toNode(json);
		return fromNodeToList(node, elementClazz);
	}

	public static <T> List<T> fromNodeToList(JsonNode node, Class<T> elementClazz) {
		Preconditions.checkNotNull(node);
		Preconditions.checkNotNull(elementClazz);

		try {
			return mapper.readValue(node.traverse(), TypeFactory.defaultInstance().constructCollectionType(List.class, elementClazz));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Set<T> fromJsonToSet(String json, Class<T> elementClazz) {
		Preconditions.checkNotNull(json);
		Preconditions.checkNotNull(elementClazz);

		JsonNode node = toNode(json);
		return fromNodeToSet(node, elementClazz);
	}

	public static <T> Set<T> fromNodeToSet(JsonNode node, Class<T> elementClazz) {
		Preconditions.checkNotNull(node);
		Preconditions.checkNotNull(elementClazz);

		try {
			return mapper.readValue(node.traverse(), TypeFactory.defaultInstance().constructCollectionType(Set.class, elementClazz));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T, E> Map<T, E> fromJsonToMap(String json, Class<T> keyClazz, Class<E> valueClazz) {
		Preconditions.checkNotNull(json);
		Preconditions.checkNotNull(keyClazz);
		Preconditions.checkNotNull(valueClazz);

		JsonNode node = toNode(json);
		return fromNodeToMap(node, keyClazz, valueClazz);
	}

	public static <T, E> Map<T, E> fromNodeToMap(JsonNode node, Class<T> keyClazz, Class<E> valueClazz) {
		Preconditions.checkNotNull(node);
		Preconditions.checkNotNull(keyClazz);
		Preconditions.checkNotNull(valueClazz);

		try {
			return mapper.readValue(node.traverse(), TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Object -> Json
	 *
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		Preconditions.checkNotNull(obj);
		try {
			return mapper.writeValueAsString(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * List<T> -> List<String(Json)>
	 *
	 * @param list
	 * @return
	 */
	public static <T> List<String> toJsonList(List<T> list) {
		Preconditions.checkNotNull(list);

		return Lists.newArrayList(Lists.transform(list, new Function<T, String>() {
			@Override
			public String apply(T input) {
				return Jsons.toJson(input);
			}
		}));
	}

	/**
	 * Json -> JsonNode
	 *
	 * @param json
	 * @return
	 */
	public static JsonNode toNode(String json) {
		Preconditions.checkNotNull(json);
		try {
			return mapper.readTree(json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Java -> JsonNode
	 *
	 * @param obj
	 * @return
	 */
	public static JsonNode toNode(Object obj) {
		Preconditions.checkNotNull(obj);
		return mapper.valueToTree(obj);
	}
}

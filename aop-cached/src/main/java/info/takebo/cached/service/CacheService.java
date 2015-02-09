/**
 *
 */
package info.takebo.cached.service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author takecy
 */
public interface CacheService {

	Set<String> listKeys(String pattern);

	Optional<String> get(String key);

	void delete(Set<String> keys);

	void setWithExpire(String key, String value, int expire, TimeUnit unit);
}
